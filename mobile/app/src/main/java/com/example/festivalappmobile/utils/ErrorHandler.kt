package com.example.festivalappmobile.utils

/**
 * Classe pour parser et traduire les messages d'erreur du backend
 */
object ErrorHandler {
    
    fun parseErrorMessage(rawError: String?): String {
        if (rawError == null) return "Une erreur est survenue"
        
        val lowerError = rawError.lowercase()
        
        return when {
            // Erreurs de validation du password
            lowerError.contains("8 caractères") -> {
                "Le mot de passe doit contenir au moins 8 caractères"
            }
            lowerError.contains("majuscule") || lowerError.contains("uppercase") -> {
                "Le mot de passe doit contenir au moins une majuscule (A-Z)"
            }
            lowerError.contains("minuscule") || lowerError.contains("lowercase") -> {
                "  Le mot de passe doit contenir au moins une minuscule (a-z)"
            }
            lowerError.contains("chiffre") || lowerError.contains("digit") -> {
                "  Le mot de passe doit contenir au moins un chiffre (0-9)"
            }
            lowerError.contains("caractère spécial") || lowerError.contains("^a-za-z0-9") -> {
                "  Le mot de passe doit contenir au moins un caractère spécial (!@#$%^&* etc)"
            }
            
            // Compte non validé
            lowerError.contains("validate") || 
            lowerError.contains("validation") ||
            lowerError.contains("valide") ||
            lowerError.contains("not approved") ||
            lowerError.contains("pending") -> {
                "⏳ Votre compte n'a pas encore été validé par l'administrateur. Veuillez attendre la confirmation."
            }
            
            // Mot de passe incorrect
            lowerError.contains("password") || 
            lowerError.contains("incorrect") ||
            lowerError.contains("mot de passe") -> {
                "  Mot de passe incorrect. Veuillez réessayer."
            }
            
            // Email non trouvé
            lowerError.contains("not found") || 
            lowerError.contains("does not exist") ||
            lowerError.contains("email") -> {
                "  Cet email n'existe pas ou le compte n'est pas créé."
            }
            
            // Email déjà existant
            lowerError.contains("already") || 
            lowerError.contains("exist") ||
            lowerError.contains("unique") -> {
                "  Cet email est déjà utilisé. Veuillez en choisir un autre."
            }
            
            // Erreurs réseau
            lowerError.contains("network") || 
            lowerError.contains("timeout") ||
            lowerError.contains("connection") -> {
                "  Erreur de connexion. Veuillez vérifier votre Internet."
            }
            
            // Champs obligatoires
            lowerError.contains("required") || 
            lowerError.contains("field") -> {
                "  Veuillez remplir tous les champs correctement."
            }
            
            else -> rawError
        }
    }
}
