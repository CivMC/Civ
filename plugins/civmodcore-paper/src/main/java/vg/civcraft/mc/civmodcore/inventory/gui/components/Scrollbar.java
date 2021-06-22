package vg.civcraft.mc.civmodcore.inventory.gui.components;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.LClickable;

public class Scrollbar extends InventoryComponent {

	private List<IClickable> unpaginatedContent;
	private int page;
	private int totalPages;
	private int backClickSlot;
	private int forwardClickSlot;
	private ContentAligner contentAligner;
	private int scrollOffset;

	public Scrollbar(List<IClickable> content, int staticSize) {
		this(content, staticSize, staticSize);
	}

	public Scrollbar(List<IClickable> content, int staticSize, int scrollOffset) {
		this(content, staticSize, scrollOffset, ContentAligners.getLeftAligned());
	}

	public Scrollbar(List<IClickable> content, int staticSize, int scrollOffset, ContentAligner contentAligner) {
		super(staticSize);
		if (content != null) {
			this.unpaginatedContent = content;
		} else {
			this.unpaginatedContent = new ArrayList<>();
		}
		this.page = 0;
		this.scrollOffset = scrollOffset;
		this.backClickSlot = 0;
		this.forwardClickSlot = staticSize - 1;
		this.contentAligner = contentAligner;
		this.totalPages = calculatePageAmount();
	}

	public void setBackwardsClickSlot(int backClickSlot) {
		this.backClickSlot = backClickSlot;
	}

	public void setForwardClickSlot(int forwardClickSlot) {
		this.forwardClickSlot = forwardClickSlot;
	}
	
	public void addItem(IClickable toAdd) {
		this.unpaginatedContent.add(toAdd);
		this.totalPages = calculatePageAmount();
		rebuild();
	}

	private int calculatePageAmount() {
		int contentAmount = unpaginatedContent.size();
		int displaySize = getSize();
		if (contentAmount <= displaySize) {
			// works fine, we dont need back/forward buttons in this case
			return 1;
		}
		// first page has no back button
		contentAmount -= displaySize - 1;

		// modulo scroll offset - 2, because a normal page has forward and backwards
		// buttons
		int modOffset = contentAmount % (scrollOffset);
		int basicRowCalc = contentAmount / (scrollOffset);
		if (modOffset <= 1) {
			// there would be one leftover element in a new page, but we can just put that
			// in the previous page instead of a next button
			return basicRowCalc + 1;
		}
		return basicRowCalc + 2;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		if (page < 0) {
			page = 0;
		}
		if (page >= totalPages) {
			page = totalPages - 1;
		}
		this.page = page;
	}

	@Override
	void rebuild() {
		int size = getSize();
		int contentIndex = scrollOffset * page;
		// subtract offset created through the next/previous button
		// note that the first page does not have a previous button
		if (scrollOffset == size) {
			if (page > 0) {
				contentIndex -= page;
				if (page > 1) {
					contentIndex -= page - 1;
				}
			}
		}
		contentAligner.reset();
		for (int i = 0; i < size; i++) {
			int targetSlot = contentAligner.getNext();
			if (targetSlot >= this.content.size()) {
				break;
			}
			if (page > 0 && targetSlot == backClickSlot) {
				this.content.set(targetSlot, getBackwardClick());
			} else if (page < totalPages - 1 && targetSlot == forwardClickSlot) {
				this.content.set(targetSlot, getForwardClick());
			} else {
				if (contentIndex >= unpaginatedContent.size()) {
					this.content.set(targetSlot, null);
				} else {
					this.content.set(targetSlot, unpaginatedContent.get(contentIndex));
				}
				contentIndex++;
			}
		}
	}

	private IClickable getBackwardClick() {
		return new LClickable(Material.ARROW, ChatColor.GOLD + "Show previous page", p -> {
			setPage(getPage() - 1);
			update();
		});
	}

	private IClickable getForwardClick() {
		return new LClickable(Material.ARROW, ChatColor.GOLD + "Show next page", p -> {
			setPage(getPage() + 1);
			update();
		});
	}

}
