package com.neurokim.runmetronm.metronome

enum class MetronomeToneProfile(
  val storageValue: String,
  val label: String,
  val description: String,
) {
  SOFT_WOOD(
    storageValue = "soft_wood",
    label = "소프트 우드",
    description = "낮고 둥근 클릭. 러닝 중 가장 자극이 적은 기본 톤",
  ),
  LOW_PULSE(
    storageValue = "low_pulse",
    label = "로우 펄스",
    description = "더 묵직하고 저음이 강조된 박자감",
  ),
  CLEAR_BELL(
    storageValue = "clear_bell",
    label = "클리어 벨",
    description = "상대적으로 또렷하지만 기존보다 덜 날카로운 톤",
  ),
  ;

  companion object {
    fun fromStorageValue(value: String?): MetronomeToneProfile =
      entries.firstOrNull { it.storageValue == value } ?: SOFT_WOOD
  }
}
