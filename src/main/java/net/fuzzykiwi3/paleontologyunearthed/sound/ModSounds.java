package net.fuzzykiwi3.paleontologyunearthed.sound;

import net.fuzzykiwi3.paleontologyunearthed.PaleontologyUnearthed;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final SoundEvent CHISEL_USE = registerSoundEvent("chisel_use");

    // Remember to edit the sounds.json file

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(PaleontologyUnearthed.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        PaleontologyUnearthed.LOGGER.info("Registering Mod Sounds for " + PaleontologyUnearthed.MOD_ID);
    }
}