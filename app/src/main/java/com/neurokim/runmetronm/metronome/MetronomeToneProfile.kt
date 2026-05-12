package com.neurokim.runmetronm.metronome

enum class MetronomeToneProfile(
  val storageValue: String,
  val label: String,
  val description: String,
) {
  SOFT_WOOD(
    storageValue = "soft_wood",
    label = "Soft Wood",
    description = "낮고 둥근 클릭. 러닝 중 가장 자극이 적은 기본 톤",
  ),
  LOW_PULSE(
    storageValue = "low_pulse",
    label = "Low Pulse",
    description = "더 묵직하고 저음이 강조된 박자감",
  ),
  CLEAR_BELL(
    storageValue = "clear_bell",
    label = "Clear Bell",
    description = "상대적으로 또렷하지만 기존보다 덜 날카로운 톤",
  ),
  ;

  companion object {
    fun fromStorageValue(value: String?): MetronomeToneProfile =
      entries.firstOrNull { it.storageValue == value } ?: SOFT_WOOD
  }
}
