package com.movie_recommend.controller;

import com.movie_recommend.mapper.MovieFavourMapper;
import com.movie_recommend.service.MovieFavourService;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

public class UserBasedCollaboMovieFiltering {

    @Resource
    private MovieFavourMapper movieFavourMapper;

    @Resource
    private MovieFavourService movieFavourService;
    private Map<String, Set<String>> userFavourites;
    private Map<String, List<String>> movieUsers;

    public UserBasedCollaboMovieFiltering(Map<String, Set<String>> userFavourites) {
        this.userFavourites = userFavourites;
        this.movieUsers = new HashMap<>();

        for (String user : userFavourites.keySet()) {
            Set<String> favourites = userFavourites.get(user);
            for (String movie : favourites) {
                movieUsers.computeIfAbsent(movie, k -> new ArrayList<>()).add(user);
            }
        }
    }

    public static void main(String[] args) {

        Map<String, Set<String>> favourites = new HashMap<>();
        Set<String> user1Favourites = new HashSet<>(Arrays.asList("movie1", "movie2", "movie3"));
        favourites.put("User1", user1Favourites);

        Set<String> user2Favourites = new HashSet<>(Arrays.asList("movie1", "movie2", "movie3", "movie4"));
        favourites.put("User2", user2Favourites);

        Set<String> user3Favourites = new HashSet<>(Arrays.asList("movie1", "movie4"));
        favourites.put("User3", user3Favourites);

        UserBasedCollaboMovieFiltering filter = new UserBasedCollaboMovieFiltering(favourites);

        String targetUser = "User3";
        int numRecommendations = 5;
        List<String> recommendations = filter.recommendMovies(targetUser, numRecommendations);

        System.out.println("Recommendations for " + targetUser + ":");
        for (String movie : recommendations) {
            System.out.println(movie);
        }
    }

    private double calculateSimilarity(Set<String> user1Favourites, Set<String> user2Favourites) {
        Set<String> commonMovies = new HashSet<>(user1Favourites);
        commonMovies.retainAll(user2Favourites);

        if (commonMovies.isEmpty()) {
            return 0.0;
        }

        double sum1 = commonMovies.stream().mapToInt(movie -> user1Favourites.contains(movie) ? 1 : 0).sum();
        double sum2 = commonMovies.stream().mapToInt(movie -> user2Favourites.contains(movie) ? 1 : 0).sum();
        double avg1 = sum1 / commonMovies.size();
        double avg2 = sum2 / commonMovies.size();

        double numerator = 0.0, denom1 = 0.0, denom2 = 0.0;
        for (String movie : commonMovies) {
            numerator += (user1Favourites.contains(movie) ? 1 : 0 - avg1) * (user2Favourites.contains(movie) ? 1 : 0 - avg2);
            denom1 += Math.pow(user1Favourites.contains(movie) ? 1 : 0 - avg1, 2);
            denom2 += Math.pow(user2Favourites.contains(movie) ? 1 : 0 - avg2, 2);
        }
        double similarity = numerator / (Math.sqrt(denom1) * Math.sqrt(denom2));

        return similarity;
    }

    public List<String> recommendMovies(String targetUser, int numRecommendations) {
        Map<String, Double> userSimilarities = new HashMap<>();
        for (String user : userFavourites.keySet()) {
            if (!user.equals(targetUser)) {
                double similarity = calculateSimilarity(userFavourites.get(targetUser), userFavourites.get(user));
                userSimilarities.put(user, similarity);
            }
        }

        List<String> similarUsers = userSimilarities.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(numRecommendations)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Set<String> recommendations = new HashSet<>();
        for (String user : similarUsers) {
            Set<String> favourites = userFavourites.get(user);
            favourites.stream()
                    .filter(movie -> !userFavourites.get(targetUser).contains(movie))
                    .forEach(recommendations::add);
        }

        return new ArrayList<>(recommendations);
    }
}
