package me.night0721.autochat;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import static me.night0721.autochat.AutoChat.mc;

public class SetMessageCommand extends CommandBase {

    private final String CLEAN = "" + EnumChatFormatting.RESET + EnumChatFormatting.AQUA + EnumChatFormatting.ITALIC;

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "setmessage";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/setmessage <message>";
    }

    public void displayUsage() {
        Minecraft mc = Minecraft.getMinecraft();
        mc.thePlayer.addChatMessage(new ChatComponentText(("" + EnumChatFormatting.DARK_AQUA + EnumChatFormatting.BOLD + "---------------------------------------------")));
        mc.thePlayer.addChatMessage(new ChatComponentText("Usage: " + CLEAN + "/setmessage <hub/party> <msg>"));
        mc.thePlayer.addChatMessage(new ChatComponentText(("" + EnumChatFormatting.DARK_AQUA + EnumChatFormatting.BOLD + "---------------------------------------------")));
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help") || (!args[0].equalsIgnoreCase("hub") && !args[0].equalsIgnoreCase("party"))) {
            displayUsage();
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            builder.append(args[i]).append(" ");
        }
        String message = builder.toString().trim();
        System.out.println(message);
        ConfigUtils.writeStringConfig("main", args[0], message);
        mc.thePlayer.addChatMessage(new ChatComponentText(("" + EnumChatFormatting.DARK_AQUA + EnumChatFormatting.BOLD + "---------------------------------------------" + EnumChatFormatting.RESET + "\n" + EnumChatFormatting.AQUA + EnumChatFormatting.ITALIC + "Message set to: " + EnumChatFormatting.RESET + EnumChatFormatting.AQUA + EnumChatFormatting.ITALIC + message + EnumChatFormatting.RESET + "\n" + "You have set the message to " + message + "\n" +EnumChatFormatting.DARK_AQUA + EnumChatFormatting.BOLD + "---------------------------------------------")));
    }
}