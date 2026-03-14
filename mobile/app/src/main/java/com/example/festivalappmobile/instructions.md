
> **Principe général : Clean Architecture**
> 
> L'application est découpée en 3 couches indépendantes : `data`, `domain`, `ui`.
> La règle d'or : **les dépendances vont toujours vers le bas** — `ui` dépend de `domain`, `data` dépend de `domain`, mais `domain` ne dépend de personne.

```
ui/          ← Ce que l'utilisateur voit
  │
  ▼
domain/      ← Le cœur de l'app (aucune dépendance externe)
  ▲
  │
data/        ← Les données (API, base de données locale)
```

---

## 📁 data/

> **Rôle :** Tout ce qui touche aux données — récupérer depuis l'API, stocker en local.

La couche `data` est la seule à savoir **comment** obtenir les données. Elle peut les chercher sur internet (remote) ou dans la base de données du téléphone (local).

---

### 📁 data/remote/

> **Rôle :** Communiquer avec l'API distante via Retrofit.

C'est la couche réseau. Elle contient tout ce qui est nécessaire pour faire des appels HTTP vers ton API (`https://api-festival-app.ferhatsn.fr`).

**Pourquoi c'est important ?**
Toute la logique réseau est isolée ici. Si tu changes d'API ou de librairie réseau, seul ce dossier est impacté — le reste de l'app ne bouge pas.

#### `RetrofitClient.kt`

Configure et crée l'instance Retrofit (une seule fois pour toute l'app).



#### `ApiService.kt`

Interface qui liste tous les endpoints disponibles. Chaque fonction = un appel HTTP.



#### `dto/` — Data Transfer Objects

Classes qui représentent **exactement** ce que l'API envoie et reçoit (le format JSON).

```kotlin
// Ce qu'on ENVOIE à l'API
data class LoginRequestDto(
    val email: String,
    val password: String
)

// Ce que l'API nous RETOURNE
data class LoginResponseDto(
    @SerializedName("access_token") val token: String,
    @SerializedName("user") val user: UserDto
)
```

**⚠️ Règle importante :** Les DTOs ne sont **jamais** utilisés directement dans l'UI ou les ViewModels. Ils sont toujours convertis en models du `domain/` via une fonction de mapping :

```kotlin
fun LoginResponseDto.toDomain(): User {
    return User(
        id = this.user.id,
        email = this.user.email,
        nom = this.user.nom,
        prenom = this.user.prenom
    )
}
```

---

### 📁 data/local/

> **Rôle :** Stocker les données sur le téléphone avec Room (base de données SQLite).

Permet à l'application de fonctionner **sans connexion internet** (Offline First). Les données sont sauvegardées localement et synchronisées avec l'API quand la connexion revient.



---

### 📁 data/repository/

> **Rôle :** Implémenter les interfaces du `domain/repository/` — faire le pont entre `remote` et `local`.

C'est ici que se décide la stratégie : "Est-ce que je prends les données depuis l'API ou depuis la base locale ?"



---

## 📁 domain/

> **Rôle :** Le cœur de l'application. Contient les règles métier, sans aucune dépendance externe.

C'est la couche la plus importante et la plus stable. Elle ne connaît ni Retrofit, ni Room, ni Jetpack Compose. Elle définit **ce que fait** l'application, pas **comment**.

---

### 📁 domain/models/

> **Rôle :** Les classes de données pures qui représentent les concepts de l'application.

Pas d'annotations, pas de dépendances — juste du Kotlin pur.

```kotlin
// domain/models/User.kt
data class User(
    val id: String,
    val email: String,
    val nom: String,
    val prenom: String,
    val token: String
)

// domain/models/Festival.kt
data class Festival(
    val id: String,
    val nom: String,
    val description: String,
    val dateDebut: String,
    val dateFin: String,
    val lieu: String
)
```

**Pourquoi ne pas utiliser les DTOs ou les entités Room directement ?**

```
API change "token" → "access_token"

❌ Sans model domain :
   LoginResponseDto utilisé partout → tout le code casse

✅ Avec model domain :
   Seul le mapping DTO → User change
   Le reste de l'app ne voit rien
```

Les models du `domain/` sont le **contrat stable** de l'application.

---

### 📁 domain/repository/

> **Rôle :** Les interfaces (contrats) que les repositories doivent respecter.

Ce dossier ne contient **que des interfaces** — pas d'implémentation. C'est le principe "D" de SOLID : l'inversion de dépendance.

```kotlin
// domain/repository/AuthRepository.kt
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout()
}
```

**Pourquoi une interface ici ?**
Le `domain/` ne sait pas si les données viennent de l'API, de Room, ou d'un fichier. Il définit juste **ce qu'il veut** — l'implémentation concrète est dans `data/repository/`.

Avantage concret : pour les tests, tu peux créer un `FakeAuthRepository` qui renvoie des données fictives, sans toucher à Retrofit ni Room.

---

### 📁 domain/usecases/

> **Rôle :** Une classe = une action métier précise.

Les UseCases orchestrent les appels aux repositories et appliquent les règles métier.



**Pourquoi ne pas appeler le repository directement depuis le ViewModel ?**

Si demain tu dois valider l'email, vérifier si l'utilisateur est banni, ou logger la connexion — tu modifies uniquement le `LoginUseCase`, pas le ViewModel.

---

## 📁 ui/

> **Rôle :** Tout ce que l'utilisateur voit et avec quoi il interagit.

La couche `ui` ne connaît que le `domain/`. Elle ne sait pas comment les données sont récupérées.

---

### 📁 ui/screens/

> **Rôle :** Les écrans Jetpack Compose de l'application.

Chaque fichier = un écran. Les Composables reçoivent des données et remontent des événements — ils ne contiennent aucune logique métier.



---

### 📁 ui/viewmodels/

> **Rôle :** Gérer l'état de l'écran et faire le lien entre l'UI et les UseCases.

Un ViewModel par écran. Il survit aux rotations d'écran et expose un état que le Composable observe.



---

### 📁 ui/components/

> **Rôle :** Les éléments d'interface réutilisables dans plusieurs écrans.

Boutons, champs texte, cartes, barres de navigation — tout élément utilisé plus d'une fois va ici.



---

## 📁 di/ (Injection de dépendances)

> **Rôle :** Créer et fournir les instances de classes à ceux qui en ont besoin (avec Hilt).

Sans injection de dépendances, tu devrais créer manuellement chaque instance :
```kotlin
// Sans DI — très verbeux et difficile à tester
val apiService = RetrofitClient.instance
val repository = AuthRepositoryImpl(apiService)
val useCase = LoginUseCase(repository)
val viewModel = LoginViewModel(useCase)
```

Avec Hilt, tout est automatique :
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService = RetrofitClient.instance

    @Provides
    fun provideAuthRepository(api: ApiService): AuthRepository =
        AuthRepositoryImpl(api)
}
```

---

## Flux complet — exemple Login

```
1. LoginScreen       → L'utilisateur clique sur "Se connecter"
2. LoginViewModel    → Appelle loginUseCase(email, password)
3. LoginUseCase      → Valide les données, appelle authRepository.login()
4. AuthRepositoryImpl→ Appelle apiService.login()
5. ApiService        → POST /auth/login → API
6. API               → Retourne { "access_token": "...", "user": {...} }
7. LoginResponseDto  → Converti en User via .toDomain()
8. User              → Remonte jusqu'au ViewModel
9. LoginViewModel    → Met à jour uiState (isSuccess = true)
10. LoginScreen      → Navigue vers l'écran suivant
```

> **Règle d'or :** `domain/` ne dépend de rien. Tout dépend de `domain/`.