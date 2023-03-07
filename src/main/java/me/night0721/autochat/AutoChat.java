package me.night0721.autochat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = "autochat", name = "AutoChat", version = "0.0.1", clientSideOnly = true)
public class AutoChat {
    public static Minecraft mc = Minecraft.getMinecraft();
    private final KeyBinding[] keyBindings = new KeyBinding[1];
    private boolean open = false;
    private State state = State.IDLE;
    private final Rotation rotation = new Rotation();
    private final Clock clock = new Clock();
    private int slot = 10;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ConfigUtils.register();
        keyBindings[0] = new KeyBinding("Toggle", Keyboard.KEY_J, "AutoChat");
        for (KeyBinding keyBinding : keyBindings) {
            ClientRegistry.registerKeyBinding(keyBinding);
        }
        ClientCommandHandler.instance.registerCommand(new SetMessageCommand());
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();
        for (String s : message.split("\n")) {
            if (s.contains("has invited you to join their party!")) {
                if (state != State.IDLE) toggle();
                new Thread(() -> {
                    try {
                        String playerName = s.split("has invited you to join their party!")[0];
                        if (playerName.contains("]")) playerName = playerName.split("]")[1].trim();
                        else playerName = playerName.trim();
                        Thread.sleep(2000);
                        mc.thePlayer.sendChatMessage("/p accept " + playerName);
                        mc.thePlayer.sendChatMessage("/pc " + ConfigUtils.getString("main", "party"));
                        Thread.sleep(2000);
                        mc.thePlayer.sendChatMessage("/p leave");
                        toggle();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (open) {
            switch (state) {
                case IDLE:
                    rotation.completed = true;
                    stopMovement();
                    break;
                case ROTATE:
                    if (clock.passed()) {
                        if (AngleUtils.smallestAngleDifference(AngleUtils.get360RotationYaw(), 90f) > 0.5) {
                            rotation.easeTo(90, 0, 1000);
                        } else {
                            state = State.MOVE;
                        }
                    }
                    break;
                case MOVE:
                    if (clock.passed()) {
                        if (distance() < 0.6f) {
                            stopMovement();
                            state = State.ROTATE2;
                        } else updateKeys(true, false, false, false, false, true, false);
                    }
                    break;
                case ROTATE2:
                    if (AngleUtils.smallestAngleDifference(AngleUtils.get360RotationYaw(), 30f) > 0.5) {
                        rotation.easeTo(30, 0, 1000);
                    } else {
                        state = State.SEND;
                    }
                    break;
                case SEND:
                    System.out.println(ConfigUtils.getString("main", "hub"));
                    mc.thePlayer.sendChatMessage("/ac " + ConfigUtils.getString("main", "hub"));
                    clock.schedule(1000);
                    state = State.OPEN;
                    break;
                case OPEN:
                    if (clock.passed()) {
                        mc.playerController.interactWithEntitySendPacket(mc.thePlayer, getSelector());
                        clock.schedule(1000);
                        state = State.CLICK;
                    }
                    break;
                case CLICK:
                    if (clock.passed()) {
                        mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot++, 0, 0, mc.thePlayer);
                        if (slot == 44) slot = 10;
                        clock.schedule(5000);
                        state = State.ROTATE;
                    }
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (rotation.rotating) {
            rotation.update();
        }
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (keyBindings[0].isPressed()) {
            toggle();
        }
    }

    public static void updateKeys(boolean forward, boolean back, boolean right, boolean left, boolean attack, boolean crouch, boolean space) {
        if (mc.currentScreen != null) {
            stopMovement();
            return;
        }
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), forward);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), back);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), right);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), left);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), attack);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), crouch);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), space);
    }

    public static void stopMovement() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);

    }

    public void start() {
        mc.thePlayer.addChatMessage(new ChatComponentText("Started!"));
        state = State.ROTATE;
        clock.schedule(1000);
    }

    public void stop() {
        mc.thePlayer.addChatMessage(new ChatComponentText("Stopped!"));
        state = State.IDLE;
        stopMovement();
    }

    public void toggle() {
        open = !open;
        if (open) {
            mc.thePlayer.sendChatMessage("/hub");
            start();
        } else {
            stop();
        }
    }

    private Entity getSelector() {
        for (final Entity e : mc.theWorld.loadedEntityList) {
            if (e instanceof EntityArmorStand) {
                final String name = StringUtils.stripControlCodes(e.getDisplayName().getUnformattedText());
                if (name.startsWith("Hub Selector")) {
                    return e;
                }
            }
        }
        return null;
    }

    private float distance() {
        return (float) Math.sqrt(Math.pow(mc.thePlayer.posX - (-9), 2) + Math.pow(mc.thePlayer.posZ - (-69), 2));
    }
}

enum State {
    IDLE, ROTATE, ROTATE2, MOVE, SEND, OPEN, CLICK
}