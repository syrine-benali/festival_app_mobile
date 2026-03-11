# festival_app_mobile


# Organisation de l'arborescense de la partie front

📁 data : Contiendra tout ce qui touche aux données (locale et distante).

    📁 local : Base de données Room (Offline first), DAO.

    📁 remote : Retrofit, ApiService.

    📁 repository : L'implémentation de vos repositories (qui feront le pont entre local et remote).

📁 domain:

    📁 models : Les classes de données pures (ex: Festival, User).

    📁 repository : Les interfaces de vos repositories (Pour le "D" de SOLID : Inversion de dépendance).

📁 ui :

    📁 screens : Vos écrans Compose (LoginScreen, FestivalListScreen).

    📁 components : Vos éléments réutilisables (Cartes, boutons, AppBar).

    📁 theme : Déjà créé par Android Studio (Couleurs Material Design 3, Typographie).