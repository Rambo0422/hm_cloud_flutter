package com.sayx.hm_cloud.model

import java.io.Serializable

class SpecificArchive : Serializable {
    var uploadArchive: Boolean = false
    var isThirdParty: Boolean = false
    var gameId: String = ""
    var downloadUrl: String = ""
    var md5: String = ""
    var cid: Long = 0
    var format: String = ""
    var source: String = ""
}
