// language: Java, file: XqtzClient.java, target: Minecraft 1.21.11, Fabric
package vanta;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class XqtzClient implements ClientModInitializer {
    private static KeyBinding openGui;

    @Override
    public void onInitializeClient() {
        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xqtz.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.xqtz"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new XqtzScreen());
                }
            }
            if (client.player != null && client.world != null) {
                for (Module m : modules) {
                    if (m.enabled) m.onTick();
                }
            }
        });
    }

    public enum Category { PVP, MOVEMENT, VISUAL, UTILITY }

    public static class Setting {
        public String name;
        public boolean value;
        public boolean isSlider;
        public float min, max, current;

        public Setting(String name, boolean val) { this.name = name; this.value = val; this.isSlider = false; }
        public Setting(String name, float min, float max, float val) { 
            this.name = name; this.min = min; this.max = max; this.current = val; this.isSlider = true; 
        }
    }

    public static abstract class Module {
        public final String name;
        public final Category category;
        public boolean enabled = false;
        public boolean expanded = false;
        public List<Setting> settings = new ArrayList<>();

        public Module(String name, Category category) {
            this.name = name;
            this.category = category;
        }

        public void toggle() { 
            enabled = !enabled; 
            if (enabled) onEnable(); else onDisable();
        }
        
        public void onEnable() {}
        public void onDisable() {}
        public void onTick() {}
    }

    public static final List<Module> modules = new ArrayList<>();

    static {
        modules.add(new Module("Auto", Category.PVP) {});
        modules.add(new Module("Auto Crystal", Category.PVP) {});
        modules.add(new Module("Auto Web", Category.PVP) {});
        modules.add(new Module("Criticals", Category.PVP) {});
        modules.add(new Module("Hole Filler", Category.PVP) {});
        modules.add(new Module("Surround Miner", Category.PVP) {});
        modules.add(new Module("Trap", Category.PVP) {});
        
        modules.add(new Module("Mace", Category.PVP) {
            { settings.add(new Setting("Auto Attack", false)); }
            @Override
            public void onTick() {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player == null || mc.world == null) return;
                if (mc.player.getMainHandStack().isOf(Items.MACE) && mc.player.fallDistance > 1.5f && mc.player.getAttackCooldownProgress(0) >= 1.0f) {
                    if (mc.targetedEntity instanceof LivingEntity && mc.targetedEntity.distanceTo(mc.player) < 4.5) {
                        mc.interactionManager.attackEntity(mc.player, (LivingEntity) mc.targetedEntity);
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                }
            }
        });

        modules.add(new Module("Anti Knock Back", Category.MOVEMENT) {
            @Override public void onTick() { 
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null) { mc.player.setVelocity(mc.player.getVelocity().multiply(0.6, 1.0, 0.6)); } 
            }
        });
        modules.add(new Module("Avoid", Category.MOVEMENT) {});
        modules.add(new Module("Elytra Recase", Category.MOVEMENT) {});
        modules.add(new Module("Gui Move", Category.MOVEMENT) {
            @Override public void onTick() {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.currentScreen != null && mc.player != null) {
                    if (mc.options.forwardKey.isPressed()) mc.player.input.movementForward = 1.0f;
                    if (mc.options.backKey.isPressed()) mc.player.input.movementForward = -1.0f;
                    if (mc.options.leftKey.isPressed()) mc.player.input.movementSideways = 1.0f;
                    if (mc.options.rightKey.isPressed()) mc.player.input.movementSideways = -1.0f;
                }
            }
        });
        modules.add(new Module("Hole Anchor", Category.MOVEMENT) {});
        modules.add(new Module("Move Fix", Category.MOVEMENT) {});
        modules.add(new Module("No Push", Category.MOVEMENT) {});
        modules.add(new Module("Sprint", Category.MOVEMENT) {
            @Override public void onTick() { if (MinecraftClient.getInstance().player != null) MinecraftClient.getInstance().options.sprintKey.setPressed(true); }
        });

        modules.add(new Module("Aspect", Category.VISUAL) {});
        modules.add(new Module("Crystal Charms", Category.VISUAL) {});
        modules.add(new Module("Free Look", Category.VISUAL) {});
        modules.add(new Module("Full Bright", Category.VISUAL) {
            @Override public void onTick() { MinecraftClient.getInstance().options.getGamma().setValue(10.0); }
            @Override public void onDisable() { MinecraftClient.getInstance().options.getGamma().setValue(1.0); }
        });
        modules.add(new Module("Hand Charms", Category.VISUAL) {});
        modules.add(new Module("Hit Fix", Category.VISUAL) {});
        modules.add(new Module("No Camera Clip", Category.VISUAL) {});
        modules.add(new Module("Pop Charms", Category.VISUAL) {});
        modules.add(new Module("Swing Animations", Category.VISUAL) {});
        modules.add(new Module("Totem Animation", Category.VISUAL) {});
        modules.add(new Module("Trajectories", Category.VISUAL) {});
        modules.add(new Module("View Model", Category.VISUAL) {});
        modules.add(new Module("World Renderer", Category.VISUAL) {});

        modules.add(new Module("Auto Bone Meal", Category.UTILITY) {});
        modules.add(new Module("Auto Land", Category.UTILITY) {});
        modules.add(new Module("Auto Shear", Category.UTILITY) {});
        modules.add(new Module("Auto Sign", Category.UTILITY) {});
        modules.add(new Module("Fake Player", Category.UTILITY) {});
        modules.add(new Module("Free Cam", Category.UTILITY) {});
        modules.add(new Module("No Render", Category.UTILITY) {});
        modules.add(new Module("Nuker", Category.UTILITY) {});
        modules.add(new Module("Pearl Chaser", Category.UTILITY) {});
        modules.add(new Module("Soul Ripper", Category.UTILITY) {});
        modules.add(new Module("Tps Sync", Category.UTILITY) {});
    }

    public static List<Module> getModules(Category c) {
        return modules.stream().filter(m -> m.category == c).toList();
    }

    public static class XqtzScreen extends Screen {
        private static final int PANEL_W = 110;
        private static final int PANEL_H = 260;
        private static final int GAP = 8;
        private static final int HEADER_H = 20;
        private static final int ROW_H = 16;

        private static final int BG        = 0xEE101114;
        private static final int HEADER    = 0xFF1E2128;
        private static final int ACCENT    = 0xFFE86060; 
        private static final int TEXT_MAIN = 0xFFE8EAED;
        private static final int TEXT_DIM  = 0xFF8A8F98;
        private static final int TEXT_ON   = 0xFFFFFFFF;
        private static final int ROW_HOVER = 0xFF282C34;
        private static final int ROW_ACTIVE= 0x44E86060;

        public XqtzScreen() {
            super(Text.literal("Xqtz"));
        }

        @Override
        public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
            renderBackground(ctx, mouseX, mouseY, delta);

            int startX = (width - ((PANEL_W * 4) + (GAP * 3))) / 2;
            int startY = (height - PANEL_H) / 2;

            Category[] cats = Category.values();
            for (int i = 0; i < cats.length; i++) {
                Category cat = cats[i];
                int px = startX + (i * (PANEL_W + GAP));
                
                ctx.fill(px, startY, px + PANEL_W, startY + PANEL_H, BG);
                ctx.fill(px, startY, px + PANEL_W, startY + HEADER_H, HEADER);
                ctx.drawCenteredTextWithShadow(textRenderer, cat.name(), px + (PANEL_W / 2), startY + 6, ACCENT);
                
                List<Module> mods = getModules(cat);
                int ry = startY + HEADER_H + 4;
                
                for (Module m : mods) {
                    boolean hover = mouseX >= px && mouseX <= px + PANEL_W && mouseY >= ry && mouseY <= ry + ROW_H;
                    
                    if (m.enabled) {
                        ctx.fill(px + 2, ry, px + PANEL_W - 2, ry + ROW_H, ROW_ACTIVE);
                    } else if (hover) {
                        ctx.fill(px + 2, ry, px + PANEL_W - 2, ry + ROW_H, ROW_HOVER);
                    }

                    ctx.drawText(textRenderer, m.name, px + 6, ry + 4, m.enabled ? TEXT_ON : TEXT_DIM, false);
                    
                    if (!m.settings.isEmpty()) {
                        ctx.drawText(textRenderer, m.expanded ? "-" : "+", px + PANEL_W - 12, ry + 4, TEXT_DIM, false);
                    }

                    ry += ROW_H;

                    if (m.expanded) {
                        for (Setting s : m.settings) {
                            if (s.isSlider) {
                                ctx.drawText(textRenderer, s.name + ": " + String.format("%.1f", s.current), px + 10, ry + 4, TEXT_DIM, false);
                            } else {
                                ctx.drawText(textRenderer, s.name, px + 10, ry + 4, s.value ? ACCENT : TEXT_DIM, false);
                            }
                            ry += ROW_H;
                        }
                    }
                }
            }
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            int startX = (width - ((PANEL_W * 4) + (GAP * 3))) / 2;
            int startY = (height - PANEL_H) / 2;
            Category[] cats = Category.values();

            for (int i = 0; i < cats.length; i++) {
                int px = startX + (i * (PANEL_W + GAP));
                List<Module> mods = getModules(cats[i]);
                int ry = startY + HEADER_H + 4;

                for (Module m : mods) {
                    if (mx >= px && mx <= px + PANEL_W && my >= ry && my <= ry + ROW_H) {
                        if (button == 0) {
                            m.toggle();
                        } else if (button == 1 && !m.settings.isEmpty()) {
                            m.expanded = !m.expanded;
                        }
                        return true;
                    }
                    ry += ROW_H;

                    if (m.expanded) {
                        for (Setting s : m.settings) {
                            if (mx >= px && mx <= px + PANEL_W && my >= ry && my <= ry + ROW_H) {
                                if (!s.isSlider && button == 0) s.value = !s.value;
                                return true;
                            }
                            ry += ROW_H;
                        }
                    }
                }
            }
            return super.mouseClicked(mx, my, button);
        }

        @Override
        public boolean shouldPause() { return false; }
    }
}
