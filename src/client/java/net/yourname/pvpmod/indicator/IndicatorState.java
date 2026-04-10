package net.yourname.pvpmod.indicator;

import net.minecraft.entity.player.PlayerEntity;
import net.yourname.pvpmod.config.ModConfig;

// ✅ PUBLIC class in its own file
public class IndicatorState {
    public float currentHealth;
    public float targetHealth;
    public long lastChangeTime;
    public boolean isIncreasing;
    public boolean isShaking;
    public float shakeOffset;
    
    public IndicatorState(PlayerEntity player) {
        targetHealth = Math.max(0, player.getHealth()) / Math.max(1, player.getMaxHealth());
        currentHealth = targetHealth;
    }
    
    public void update(PlayerEntity player, ModConfig cfg) {
        targetHealth = Math.max(0, player.getHealth()) / Math.max(1, player.getMaxHealth());
        
        if (Math.abs(currentHealth - targetHealth) > 0.001f) {
            isIncreasing = targetHealth > currentHealth;
            lastChangeTime = System.currentTimeMillis();
            if (!isIncreasing && cfg.sparkBreakAnimation) {
                isShaking = true;
                shakeOffset = 0;
            }
        }
        
        if (cfg.smoothAnimation) {
            currentHealth = currentHealth + cfg.animationSpeed * (targetHealth - currentHealth);
        } else {
            currentHealth = targetHealth;
        }
        
        if (isShaking) {
            shakeOffset += 0.5f;
            if (shakeOffset > 30) {
                isShaking = false;
                shakeOffset = 0;
            }
        }
    }
    
    public float getRenderHealth() {
        if (isShaking) {
            return currentHealth + (float)(Math.sin(shakeOffset * 0.3) * 0.02f);
        }
        return currentHealth;
    }
}
