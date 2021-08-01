package com.jovines.autohandleleave.bean

data class SchoolLeavingApproval(
    val `data`: Data? = null,
    val message: String? = null,
    val status: Int
) {
    data class Data(
        val result: List<Result>? = null
    )

    data class Result(
        val bz: String?,
        val created_at: String?,
        val fdy: String?,
        val fdyspjg: Any,
        val fdyspsj: Any,
        val fdyyj: Any,
        val fdyzgh: String?,
        val fgld: String?,
        val fgldzgh: String?,
        val fsjspjg: Any,
        val fsjspsj: Any,
        val fsjyj: String?,
        val gxcz: String?,
        val gxsj: String?,
        val lczt: String?,
        val lcztdm: String?,
        val log_id: String?,
        val lxsmdd: Any,
        val lxsmsj: Any,
        val name: String?,
        val nj: String?,
        val qjlx: String?,
        val qjsy: String?,
        val qk: Any,
        val rxsmdd: Any,
        val rxsmsj: Any,
        val sfasfx: Any,
        val spfdy: Any,
        val spfdygh: Any,
        val spfsj: Any,
        val spfsjgh: Any,
        val wcmdd: String?,
        val wcrq: String?,
        val wcxxdd: String?,
        val xh: String?,
        val xy: String?,
        val yjfxsj: String?
    )
}