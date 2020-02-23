package com.github.yuttyann.scriptblockplus.script.option.chat;

import org.bukkit.Bukkit;

import com.github.yuttyann.scriptblockplus.script.option.BaseOption;
import com.github.yuttyann.scriptblockplus.script.option.Option;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

public class Server extends BaseOption {

	public Server() {
		super("server", "@server ");
	}

	@NotNull
	@Override
	public Option newInstance() {
		return new Server();
	}

	@Override
	protected boolean isValid() throws Exception {
		Bukkit.broadcastMessage(StringUtils.replaceColorCode(getOptionValue(), true));
		return true;
	}
}