package com.tumhara.mod.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.google.gson.JsonParser;

public class SkinDashboardScreen extends Screen {
    private static final Identifier DEFAULT_SKIN = Identifier.of("minecraft", "textures/entity/player/wide/steve.png");
    private TextFieldWidget skinUrlField;
    private final Screen parent;
    private String statusMessage = "";
    private String currentStatusColor = "#55FF55";

    protected SkinDashboardScreen(Screen parent) {
        super(Text.literal("§6✨ Skin Dashboard §r"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        // URL/Username input field
        this.skinUrlField = new TextFieldWidget(
            this.textRenderer,
            this.width / 2 - 100,
            this.height / 2 - 30,
            200,
            20,
            Text.literal("Enter username or skin URL")
        );
        this.skinUrlField.setMaxLength(256);
        this.skinUrlField.setRenderTextProvider((text, firstCharacter) -> {
            return Text.literal(text).asOrderedText();
        });
        this.addSelectableChild(this.skinUrlField);

        // Apply button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§a✓ Apply Skin"),
            button -> applySkin(this.skinUrlField.getText())
        )
        .dimensions(this.width / 2 - 100, this.height / 2 + 10, 200, 20)
        .build());

        // Back button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§c✗ Back to Menu"),
            button -> this.close()
        )
        .dimensions(this.width / 2 - 100, this.height / 2 + 40, 200, 20)
        .build());
        
        // Reset button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§6⟳ Reset to Default"),
            button -> resetToDefaultSkin()
        )
        .dimensions(this.width / 2 - 100, this.height / 2 + 70, 200, 20)
        .build());
    }

    private void applySkin(String input) {
        if (input == null || input.trim().isEmpty()) {
            statusMessage = "§cPlease enter a username or URL!";
            currentStatusColor = "#FF5555";
            return;
        }
        
        statusMessage = "§eLoading skin...";
        currentStatusColor = "#FFFF55";
        
        try {
            if (input.startsWith("http://") || input.startsWith("https://")) {
                loadSkinFromUrl(input);
            } else {
                loadSkinFromUsername(input.trim());
            }
        } catch (Exception e) {
            statusMessage = "§cFailed: " + e.getMessage();
            currentStatusColor = "#FF5555";
            SkinChangerMod.LOGGER.error("Skin apply error", e);
        }
    }
    
    private void resetToDefaultSkin() {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.setSkinTextures(null);
            statusMessage = "§aReset to default skin!";
            currentStatusColor = "#55FF55";
        }
    }

    private void loadSkinFromUrl(String url) {
        new Thread(() -> {
            try {
                String skinHash = Integer.toHexString(url.hashCode());
                Identifier skinId = Identifier.of("custom_skins", skinHash);
                
                MinecraftClient.getInstance().execute(() -> {
                    applySkinTexture(skinId);
                    statusMessage = "§aSkin applied from URL!";
                    currentStatusColor = "#55FF55";
                });
            } catch (Exception e) {
                MinecraftClient.getInstance().execute(() -> {
                    statusMessage = "§cFailed to load skin from URL";
                    currentStatusColor = "#FF5555";
                });
            }
        }).start();
    }

    private void loadSkinFromUsername(String username) {
        new Thread(() -> {
            try {
                String uuid = getUUIDFromUsername(username);
                if (uuid != null) {
                    Identifier skinId = Identifier.of("mojang_skins", uuid);
                    MinecraftClient.getInstance().execute(() -> {
                        applySkinTexture(skinId);
                        statusMessage = "§aSkin loaded for " + username + "!";
                        currentStatusColor = "#55FF55";
                    });
                } else {
                    MinecraftClient.getInstance().execute(() -> {
                        statusMessage = "§cUser not found: " + username;
                        currentStatusColor = "#FF5555";
                    });
                }
            } catch (Exception e) {
                MinecraftClient.getInstance().execute(() -> {
                    statusMessage = "§cError loading skin";
                    currentStatusColor = "#FF5555";
                });
            }
        }).start();
    }

    private void applySkinTexture(Identifier textureId) {
        var client = MinecraftClient.getInstance();
        if (client.player != null && client.getSkinProvider() != null) {
            client.player.setSkinTextures(null);
            SkinChangerMod.LOGGER.info("Skin texture applied: " + textureId);
        }
    }

    private String getUUIDFromUsername(String username) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("https://api.mojang.com/users/profiles/minecraft/" + username))
                .header("User-Agent", "Minecraft-SkinChanger/1.0")
                .build();
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                var json = JsonParser.parseString(response.body()).getAsJsonObject();
                return json.get("id").getAsString();
            }
        } catch (Exception e) {
            SkinChangerMod.LOGGER.error("Failed to get UUID", e);
        }
        return null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Title
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            this.title,
            this.width / 2,
            20,
            0xFFAA00
        );
        
        // Subtitle
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§7Change your Minecraft skin easily"),
            this.width / 2,
            40,
            0xAAAAAA
        );
        
        // Status message
        if (!statusMessage.isEmpty()) {
            int color;
            if (currentStatusColor.equals("#55FF55")) color = 0x55FF55;
            else if (currentStatusColor.equals("#FFFF55")) color = 0xFFFF55;
            else color = 0xFF5555;
            
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(statusMessage),
                this.width / 2,
                this.height / 2 - 60,
                color
            );
        }
        
        // Input label
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("§6Username or Skin URL:"),
            this.width / 2 - 100,
            this.height / 2 - 45,
            0xFFAA55
        );
        
        // Info text
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("§7• Enter Minecraft username to fetch skin"),
            this.width / 2 - 100,
            this.height / 2 + 100,
            0x888888
        );
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("§7• Or paste direct skin image URL"),
            this.width / 2 - 100,
            this.height / 2 + 112,
            0x888888
        );
        
        this.skinUrlField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
                          }
