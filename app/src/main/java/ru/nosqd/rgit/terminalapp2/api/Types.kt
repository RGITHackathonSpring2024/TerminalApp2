package ru.nosqd.rgit.terminalapp2.api

// Enumerations for Account and Card states with explicit string values
enum class AccountState(val value: String) {
    EMAIL_VERIFICATION("EMAIL_VERIFICATION"),
    DOCUMENT_VERIFICATION("DOCUMENT_VERIFICATION"),
    VERIFIED("VERIFIED"),
    DISABLED("DISABLED")
}

enum class CardState(val value: String) {
    ACTIVE("ACTIVE"),
    BLOCKED("BLOCKED")
}

// Data classes for User, Card, Institution, Transaction, Application, and EmailCode
data class User(
    val id: String,
    val email: String,
    val login: String,
    val password: String?,
    val firstName: String,
    val lastName: String,
    val dadName: String,
    val accountState: AccountState,
    val card: Card?,
    val application: Array<Application>?,
    val emailCode: Array<EmailCode>?
)

data class Card(
    val id: String,
    val balance: Double,
    val expiration: String,
    val permanentAccountNumber: String,
    val userId: String,
    val user: User?,
    val transaction: Array<Transaction>?,
    val lastToken: String
)

data class Institution(
    val id: String,
    val name: String,
    val transactionsAccepted: Array<Transaction>?
)

data class Transaction(
    val id: String,
    val amount: Double,
    val date: String,
    val toInstitution: Institution?,
    val card: Card?,
    val cardId: String,
    val institutionId: String
)

data class Application(
    val id: String,
    val user: User?,
    val userId: String?
)

data class EmailCode(
    val id: String,
    val user: User?,
    val userId: String?,
    val code: String,
    val isActivated: Boolean
)