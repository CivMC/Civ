package com.untamedears.JukeAlert.command;

import com.untamedears.JukeAlert.JukeAlert;

public abstract class PlayerCommand implements Command
{
	protected JukeAlert plugin = JukeAlert.getInstance();
	
	private String name;
	private String description = "";
	private String usage = "";
	private int minArguments = 0;
	private int maxArguments = 0;
	private String identifier = "";

	public PlayerCommand(String name) 
	{
		this.name = name;
	}

	public String getName() 
	{
		return this.name;
	}

	public String getDescription() 
	{
		return this.description;
	}

	public String getUsage() 
	{
		return this.usage;
	}

	public String getIdentifier() 
	{
		return this.identifier;
	}

	public int getMinArguments() 
	{
		return this.minArguments;
	}

	public int getMaxArguments() 
	{
		return this.maxArguments;
	}
	
	public void setArgumentRange(int min, int max)
	{
		this.minArguments = min;
		this.maxArguments = max;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setUsage(String usage)
	{
		this.usage = usage;
	}
	
	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}
	
	public void setMinArguments(int minArguments)
	{
		this.minArguments = minArguments;
	}
	
	public void setMaxArguments(int maxArguments)
	{
		this.maxArguments = maxArguments;
	}
}
