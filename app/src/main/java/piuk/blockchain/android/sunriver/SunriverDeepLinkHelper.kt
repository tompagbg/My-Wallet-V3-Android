package piuk.blockchain.android.sunriver

import android.content.Intent
import android.net.Uri
import com.blockchain.kyc.models.nabu.CampaignData
import com.blockchain.notifications.links.PendingLink
import io.reactivex.Maybe
import io.reactivex.Single

class SunriverDeepLinkHelper(
    private val linkHandler: PendingLink
) {

    fun getCampaignCode(intent: Intent): Single<CampaignLinkState> = linkHandler.getPendingLinks(intent)
        .map { uri ->
            val fragment = uri.encodedFragment?.let { Uri.parse(it) } ?: return@map CampaignLinkState.NoUri
            val name = fragment.getQueryParameter("campaign")
            val code = fragment.getQueryParameter("campaign_code")
            val email = fragment.getQueryParameter("campaign_email")
            val newUser = fragment.getQueryParameter("newUser")?.toBoolean() ?: false

            if (!name.isNullOrEmpty() && !code.isNullOrEmpty() && !email.isNullOrEmpty()) {
                CampaignLinkState.Data(CampaignData(name, code, email, newUser))
            } else {
                CampaignLinkState.WrongUri
            }
        }
        .switchIfEmpty(Maybe.just(CampaignLinkState.NoUri))
        .toSingle()
        .onErrorResumeNext { Single.just(CampaignLinkState.NoUri) }
}

sealed class CampaignLinkState {

    data class Data(val campaignData: CampaignData) : CampaignLinkState()
    object NoUri : CampaignLinkState()
    object WrongUri : CampaignLinkState()
}