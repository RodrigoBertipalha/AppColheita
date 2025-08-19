package com.colheitadecampo.data.model

data class GroupStats(
    val grupoId: String,
    val total: Int,
    val colhidos: Int
) {
    val percentageColhido: Float
        get() = if (total > 0) (colhidos * 100f / total) else 0f
}
