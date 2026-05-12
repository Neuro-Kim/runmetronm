package com.neurokim.runmetronm.metronome

enum class MetronomeToneProfile(
  val storageValue: String,
  val label: String,
  val description: String,
) {
  SOFT_WOOD(
    storageValue = "soft_wood",
    label = "Soft Wood",
    description = "Low, rounded click. The gentlest option for running.",
  ),
  LOW_PULSE(
    storageValue = "low_pulse",
    label = "Low Pulse",
    description = "Heavier feel with emphasized lows.",
  ),
  CLEAR_BELL(
    storageValue = "clear_bell",
    label = "Clear Bell",
    description = "Crisper than the others, but not piercing.",
  ),
  ;

  companion object {
    fun fromStorageValue(value: String?): MetronomeToneProfile =
      entries.firstOrNull { it.storageValue == value } ?: SOFT_WOOD
  }
}
