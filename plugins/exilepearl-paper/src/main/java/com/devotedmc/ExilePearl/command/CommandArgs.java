package com.devotedmc.ExilePearl.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class CommandArgs implements List<CommandArg> {
	
	private final List<CommandArg> args = new ArrayList<CommandArg>();
	
	public CommandArgs() {
		
	}

	@Override
	public int size() {
		return args.size();
	}

	@Override
	public boolean isEmpty() {
		return args.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return args.contains(o);
	}

	@Override
	public Iterator<CommandArg> iterator() {
		return args.iterator();
	}

	@Override
	public Object[] toArray() {
		return args.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return args.toArray(a);
	}

	@Override
	public boolean add(CommandArg e) {
		return args.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return args.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return args.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends CommandArg> c) {
		return args.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends CommandArg> c) {
		return args.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return args.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return args.retainAll(c);
	}

	@Override
	public void clear() {
		args.clear();
	}

	@Override
	public CommandArg get(int index) {
		return args.get(index);
	}

	@Override
	public CommandArg set(int index, CommandArg element) {
		return args.set(index, element);
	}

	@Override
	public void add(int index, CommandArg element) {
		args.add(index, element);
	}

	@Override
	public CommandArg remove(int index) {
		return args.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return args.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return args.lastIndexOf(o);
	}

	@Override
	public ListIterator<CommandArg> listIterator() {
		return args.listIterator();
	}

	@Override
	public ListIterator<CommandArg> listIterator(int index) {
		return args.listIterator(index);
	}

	@Override
	public List<CommandArg> subList(int fromIndex, int toIndex) {
		return args.subList(fromIndex, toIndex);
	}
	
	public void add(String str) {
		this.add(new CommandArg(str));
	}
	
	public void add(String str, AutoTab tab) {
		this.add(new CommandArg(str, tab));
	}
	
	public void addOptional(String str, String defValue) {
		this.add(new CommandArg(str, defValue));
	}
}
