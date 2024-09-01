package com.groww.scrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataComparator {
	
	List<String> commonAssets, uniqueAssets;
	
	public void compareTwoHoldingMaps(Map<String, Float> fund1, Map<String, Float> fund2) {
		commonAssets = new ArrayList<>();
		uniqueAssets = new ArrayList<>();
		for(String key: fund1.keySet()) {
			if(fund2.containsKey(key)) {
				commonAssets.add(key);
			} else {
				uniqueAssets.add(key);
			}
		}
		
		uniqueAssets.addAll(
				fund2.keySet()
				.stream()
				.filter((k) -> {return !commonAssets.contains(k);})
				.collect(Collectors.toList()));
		
		System.out.println("\nCommon Assets - " + commonAssets.size() + "\n");
		commonAssets.stream()
		.forEach(s -> System.out.println(String.format("%s %.2f %.2f",s , fund1.get(s), fund2.get(s))));
		
		System.out.println("\nUnique Assets - " + uniqueAssets.size() + "\n");
		uniqueAssets.stream()
		.forEach(System.out::println);
	}
}