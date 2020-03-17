package com.github.yuttyann.scriptblockplus.script.option.other;

import com.github.yuttyann.scriptblockplus.script.option.BaseOption;
import com.github.yuttyann.scriptblockplus.script.option.Option;
import com.github.yuttyann.scriptblockplus.utils.StreamUtils;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;
import com.github.yuttyann.scriptblockplus.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public class BlockType extends BaseOption {

	public BlockType() {
		super("blocktype", "@blocktype:");
	}

	@NotNull
	@Override
	public Option newInstance() {
		return new BlockType();
	}

	@Override
	protected boolean isValid() throws Exception {
		String[] array = StringUtils.split(getOptionValue(), ",");
		Block block = getLocation().getBlock();
		return StreamUtils.anyMatch(array, s -> equals(block, s));
	}

	private boolean equals(@NotNull Block block, @NotNull String blockType) {
		if (StringUtils.isEmpty(blockType)) {
			return false;
		}
		String[] array = StringUtils.split(blockType, ":");
		if (Calculation.REALNUMBER_PATTERN.matcher(array[0]).matches()) {
			Utils.sendMessage(getPlayer(), "§cNumerical values can not be used");
			return false;
		}
		Material type = Material.getMaterial(array[0]);
		if (type == null || !type.isBlock()) {
			return false;
		}
		byte data = array.length == 2 ? Byte.parseByte(array[1]) : 0;
		return type == block.getType() && data == getData(block);
	}

	private byte getData(@NotNull Block block) {
		@SuppressWarnings("deprecation")
		byte data = block.getData();
		return data;
	}
}