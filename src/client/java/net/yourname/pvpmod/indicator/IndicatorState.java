package net.yourname.pvpmod.indicator;

import net.minecraft.entity.player.PlayerEntity;
import net.yourname.pvpmod.config.ModConfig;

public class IndicatorState {
    public float currentHealth, targetHealth;
    public boolean isIncreasing, isShaking;
    public float shakeOffset;
    
    public IndicatorState(PlayerEntity p) {
        targetHealth = Math.max(0, p.getHealth()) / Math.max(1, p.getMaxHealth());
        currentHealth = targetHealth;
    }
    
    public void update(PlayerEntity p, ModConfig cfg) {
        targetHealth = Math.max(0, p.getHealth()) / Math.max(1, p.getMaxHealth());
        if (Math.abs(currentHealth - targetHealth) > 0.001f) {
            isIncreasing = targetHealth > currentHealth;
            if (!isIncreasing && cfg.sparkBreakAnimation) { isShaking = true; shakeOffset = 0; }
        }
        if (cfg.smoothAnimation) currentHealth += cfg.animationSpeed * (targetHealth - currentHealth);
        else currentHealth = targetHealth;
        if (isShaking) { shakeOffset += 0.5f; if (shakeOffset > 30) { isShaking = false; shakeOffset = 0; } }
    }
    public float getRenderHealth() { 
        return isShaking ? currentHealth + (float)(Math.sin(shakeOffset*0.3)*0.02f) : currentHealth; 
    }
}
