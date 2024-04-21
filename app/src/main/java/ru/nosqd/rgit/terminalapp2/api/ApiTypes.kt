package ru.nosqd.rgit.terminalapp2.api


data class ProcessCardRequest(val cardToken: String)
data class ProcessCardResponse(val cardId: String, val newToken: String)
data class ProcessTransactionRequest(val cardId: String, val institutionId: String, val amount: Int)
data class ProcessTransactionResponse(val transactionId: String?, val message: String?)
data class GetCardReponse(val card: Card)