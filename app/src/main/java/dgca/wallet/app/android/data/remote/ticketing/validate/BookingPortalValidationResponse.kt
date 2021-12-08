package dgca.wallet.app.android.data.remote.ticketing.validate

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

enum class BookingPortalValidationResponseResult {
    OK, NOK, CHK
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class BookingPortalValidationResponse(

    @field:JsonProperty("result")
    val result: BookingPortalValidationResponseResult,

    @field:JsonProperty("sub")
    val sub: String,

    @field:JsonProperty("iss")
    val iss: String,

    @field:JsonProperty("exp")
    val exp: String?,

    @field:JsonProperty("category")
    val category: List<String>?,

    @field:JsonProperty("confirmation")
    val confirmation: String,

    @field:JsonProperty("iat")
    val iat: Int,

    @field:JsonProperty("results")
    val resultValidations: List<BookingPortalValidationResponseResultItem>
)

data class BookingPortalValidationResponseResultItem(

    @field:JsonProperty("result")
    val result: BookingPortalValidationResponseResult,

    @field:JsonProperty("identifier")
    val identifier: String,

    @field:JsonProperty("details")
    val details: String,

    @field:JsonProperty("type")
    val type: String
)
