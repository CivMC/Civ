/**
 * Created by Aleksey on 11.07.2017.
 */

package isaac.bastion;

import java.util.HashSet;
import java.util.Set;

public class BastionGroup {
	private int groupId;
	private Set<Integer> allowedGroupIds;

	public BastionGroup(int groupId) {
		this.groupId = groupId;
		this.allowedGroupIds = new HashSet<>();
	}

	public int getGroupId() {
		return this.groupId;
	}

	public void addAllowedGroup(int groupId) {
		this.allowedGroupIds.add(groupId);
	}

	public boolean removeAllowedGroup(int groupId) {
		this.allowedGroupIds.remove(groupId);

		return this.allowedGroupIds.size() > 0;
	}

	public boolean isAllowedGroup(int groupId) {
		return this.allowedGroupIds.contains(groupId);
	}

	public Set<Integer> getAllowedGroupIds() {
		return this.allowedGroupIds;
	}
}
