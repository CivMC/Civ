package com.github.maxopoly.finale.misc;

public class AttributeModifier {

	private String name;
	private double value;
	private Slot slot;
	
	public AttributeModifier(String name, double value, Slot slot) {
		this.name = name;
		this.value = value;
		this.slot = slot;
	}
	
	public String getName() {
		return name;
	}
	
	public double getValue() {
		return value;
	}
	
	public Slot getSlot() {
		return slot;
	}
	
	public static AttributeModifier attackSpeed(double attackSpeed) {
		return new AttributeModifier("generic.attackSpeed", attackSpeed, Slot.MAIN_HAND);
	}
	
	public static AttributeModifier attackDamage(double attackDamage) {
		return new AttributeModifier("generic.attackDamage", attackDamage, Slot.MAIN_HAND);
	}
	
	public static AttributeModifier armour(double armour, Slot slot) {
		return new AttributeModifier("generic.armor", armour, slot);
	}
	
	public static AttributeModifier toughness(double toughness, Slot slot) {
		return new AttributeModifier("generic.armorToughness", toughness, slot);
	}
	
}
