package com.moonic.battle;

public class BattlePrint 
{
	static boolean usePrint=false;
	public static void print(String str)
	{
		if(usePrint)System.out.println(str);
	}

}
