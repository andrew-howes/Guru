import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class Guru {

	static int[] values;
	static String[] entrants;
	static int[] scores;
	static ArrayList<String[]> allPicks;
	static String[] results;
	static String[][] possibleResults;
	static File neighbors;
	static int nextMatch;
	
	public static void main(String[] args) {
		populateValues();
		nextMatch = 0;
		allPicks = new ArrayList<String[]>();
		try {
	        File inFile = new File(args[0]);
	        
	        neighbors = new File("neighbors.txt");
	        
	        BufferedReader in = new BufferedReader(new FileReader(inFile));
	        String line;
	        ArrayList<String> players = new ArrayList<String>();
	        int count = 0;
	        while ((line = in.readLine()) != null) {
	            String[] picks = line.split(", ", -1);
	            if(picks[0].equals("ACTUAL"))
	            {
	            	processResults(picks);
	            }else if(picks[0].equals("POSSIBLE"))
	            {
	            	processPossibleResults(picks);
	            }else{
	            	players.add(picks[0]);
	            	processPlayer(picks);
	            	count++;
	            }
	        }
	        entrants = new String[count];
	        players.toArray(entrants);
	        in.close();
	    } catch (IOException e) {
	        System.out.println("File Read Error: " + e.getMessage());
	    }
		scores = calculateScores(results);
		outputClosestBrackets();
		checkNext();
	}
	
	public static void checkNext()
	{
		String[] possibles = getPossibles(nextMatch);
		for(String poss : possibles)
		{
			possibleResults[nextMatch] = new String[1];
			possibleResults[nextMatch][0] = poss;
			results[nextMatch] = poss;
			scores = calculateScores(results);
			neighbors = new File(poss+".txt");
			outputClosestBrackets();
		}
		
	}
	
	public static String[] getPossibles(int match)
	{
		String[] result;
		int start;
		if(!possibleResults[match][0].equals(""))
			return possibleResults[match];
		ArrayList<String> temp = new ArrayList<String>();
		if(match < 108)
		{
			start = (match-81)*3;
		}else if(match < 117)
		{
			start = (match-108)*3+81;
		}else if(match < 120)
		{
			start = (match-117)*3+108;
		}else
		{
			start = 117;
		}
		for(int i = start; i < start+3; i++)
		{
			for(int j = 0; j < possibleResults[i].length; j++)
			{
				temp.add(possibleResults[i][j]);
			}
		}
		result = (String[]) temp.toArray();
		
		return result;
	}
	
	public static void populateValues()
	{
		values = new int[121];
		for(int i = 0; i < 121; i++)
		{
			if(i < 81)
				values[i] = 1;
			else if (i < 108)
				values[i] = 3;
			else if (i < 117)
				values[i] = 9;
			else if (i < 120)
				values[i] = 27;
			else
				values[i] = 81;
		}
	}
	
	public static void outputClosestBrackets()
	{
		try {
			FileWriter writer = new FileWriter(neighbors);
			
			String winner = neighbors.getName();
			
			winner = winner.substring(0,winner.indexOf("."));
			if(! winner.equals("neighbors"))
				System.out.println("Elims for a "+winner+" win:");
			
			writer.write("<span class=\"nocode\">\n");
			writer.write("updated through "+results[nextMatch-1]+"'s win\n");
			int[][] comparisons;
			int minscore;
			String out;
			ArrayList<Integer> minIDs = new ArrayList<Integer>();
			int[] diffmatches;
			for(int player = 0; player < entrants.length; player++)
			{
				comparisons = new int[entrants.length][3];
				for(int second = 0; second < entrants.length; second++)
				{
					comparisons[second] = getDifferenceScore(player, second);
				}
				minscore = 405;
				minIDs.clear();
				for(int i = 0; i < entrants.length; i++)
				{
					if(i != player)
					{
						//if(comparisons[i][1] < minscore)
						//if((scores[i]-scores[player]) + comparisons[i][2] < minscore)
						if((scores[player]-scores[i]) + comparisons[i][2] < 5 ||
								(scores[player]-scores[i]) + comparisons[i][2] < minscore)
						{
							if(minscore > 5)
								minIDs.clear();
							//minscore = comparisons[i][1];
							minscore = (scores[player]-scores[i]) + comparisons[i][2];
							minIDs.add(i);
						//}else if(comparisons[i][1] == minscore)
						}else if((scores[player]-scores[i]) + comparisons[i][2] == minscore)
						{
							minIDs.add(i);
						}
					}
				}
				out = "";
				writer.write(entrants[player]+"'s closest brackets:\n");
				for(Integer i : minIDs)
				{
					out += "  " + entrants[i] + " -";
					out += " total difference: " + comparisons[i][1];
					out += " current deficit: "+ (scores[i]-scores[player]); 
					out += " possible gain: " + comparisons[i][2] +"\n";
					out += "    magic number: " + (comparisons[i][2]-(scores[i]-scores[player])) + "\n";
					out += "\tdifferences: ";
					diffmatches = getDifferentMatches(player,i);
					out += Arrays.toString(diffmatches)+"\n";
					if((scores[i]-scores[player]) > comparisons[i][2])
					{
						out += "Should be dead\n";
						System.out.println(entrants[player] + " eliminated by " + entrants[i]);
					}
				}
				writer.write(out);
			}
			writer.write("</span>\n");
			writer.close();
		} catch (IOException e) {
			System.out.println("problem with output");
			System.exit(1);
		}
		//System.out.println("Done getting differences");
	}
	
	public static int[] getDifferentMatches(int first, int second)
	{
		String[] firstPicks = allPicks.get(first);
		String[] lastPicks = allPicks.get(second);
		
		ArrayList<Integer> differences = new ArrayList<Integer>();
		
		for(int i = 0; i < firstPicks.length; i++)
		{
			if(!firstPicks[i].equals(lastPicks[i]))
			{
				differences.add(i+1);
			}
		}
		int[] result = new int[differences.size()];
		for(int i = 0; i < result.length; i++)
		{
			result[i] = differences.get(i).intValue();
		}
		return result;
	}
	
	public static int[] getDifferenceScore(int first, int second)
	{
		String[] firstPicks = allPicks.get(first);
		String[] lastPicks = allPicks.get(second);
		int[] result = new int[3];
		//number of differences, point value, possible points to make up
		result[0] = result[1] = result[2] = 0;
		for(int i = 0; i < firstPicks.length; i++)
		{
			if(!firstPicks[i].equals(lastPicks[i]))
			{
				result[1] += values[i];
				result[0]++;
				if(i >= nextMatch && isValid(firstPicks[i],i))
				{
					result[2]+=values[i];
				}
			}
		}
		
		return result;
	}
	
	public static boolean isValid(String pick, int matchNum)
	{
		if(matchNum < 81)
		{
			for(int i = 0; i < possibleResults[matchNum].length; i++)
			{
				if(possibleResults[matchNum][i].equals(pick))
					return true;
			}
			return false;
		}else if(matchNum < 108)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isValid(pick, (matchNum-81)*3) ||
						isValid(pick, (matchNum-81)*3+1) ||
						isValid(pick, (matchNum-81)*3+2);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else if(matchNum < 117)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isValid(pick, (matchNum-108)*3+81) ||
						isValid(pick, (matchNum-108)*3+82) ||
						isValid(pick, (matchNum-108)*3+83);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else if(matchNum < 120)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isValid(pick, (matchNum-117)*3+108) ||
						isValid(pick, (matchNum-117)*3+109) ||
						isValid(pick, (matchNum-117)*3+110);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else
		{
			return isValid(pick, 117)||isValid(pick,118)||isValid(pick,119);
		}
	}
	

	
	public static void processPossibleResults(String[] possible)
	{
		possibleResults = new String[121][0];
		String[] parts;
		for(int i = 0; i < 121; i++)
		{
			parts = possible[i+1].split("; ");
			possibleResults[i] = parts;
		}
	}
	
	public static void processResults(String[] picks)
	{
		results = new String[121];
		for(int i = 1; i < picks.length; i++)
		{
			results[i-1] = picks[i];
			if(picks[i].equals("") && nextMatch == 0)
				nextMatch = i-1;
		}
	}
	
	public static void processPlayer(String[] picks)
	{
		String[] playerPicks = new String[picks.length-1];
		for(int i = 1; i < picks.length-1; i++)
		{
			playerPicks[i-1] = picks[i];
		}
		playerPicks[playerPicks.length-1] = 
				picks[picks.length-1].substring(0,picks[picks.length-1].indexOf(';'));
		allPicks.add(playerPicks);
	}
	
	public static int[] calculateScores(String[] results)
	{
		int[] scores = new int[entrants.length];
		//results = checkResults(preResults);
		for(int i = 0; i < results.length; i++)
		{
			if(!results[i].equals(""))
			{
				//for each player
				for(int j = 0; j < entrants.length; j++)
				{
					//if the player's pick for the match is equal to the result
					if(allPicks.get(j)[i].equals(results[i]))
					{
						//increase their points by the value of the match
						scores[j] += values[i];
					}
				}
			}else{
				break;
			}
		}
		return scores;
	}
}
