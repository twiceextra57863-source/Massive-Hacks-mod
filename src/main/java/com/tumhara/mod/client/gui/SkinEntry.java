package com.tumhara.mod.client.gui;

import net.minecraft.util.Identifier;
import java.io.File;

public class SkinEntry {
    private final String name;
    private final String path;
    private final Identifier textureId;
    private final File file;
    
    public SkinEntry(String name, String path, Identifier textureId, File file) {
        this.name = name;
        this.path = path;
        this.textureId = textureId;
        this.file = file;
    }
    
    public String getName() { return name; }
    public String getPath() { return path; }
    public Identifier getTextureId() { return textureId; }
    public File getFile() { return file; }
}
