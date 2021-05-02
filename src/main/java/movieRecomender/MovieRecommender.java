package movieRecomender;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class MovieRecommender {

    int totalReviews = 0;
    int totalProducts = 0;
    int totalUsers = 0;

    String lastUserId = "";
    String lastProductId = "";

    HashMap<String, Integer> products = new HashMap<>();
    HashMap<String, Integer> users = new HashMap<>();

    FileWriter currentFileWriter;

    public MovieRecommender(String path) throws IOException {
        this.inflate(path);
    }

    public void inflate (String path) throws IOException {
        InputStream gzipStream = new GZIPInputStream(new FileInputStream(path));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gzipStream));

        BufferedWriter bufferedWriter = this.createFile("datasetMovies.csv");

        String currentLine;
        String productLabel = "product/productId: ";
        String userLabel = "review/userId: ";
        String reviewLabel = "review/score: ";
        while ((currentLine = bufferedReader.readLine()) != null){
            if (currentLine.startsWith(productLabel)){
                String productId = currentLine.replace(productLabel, "");
                lastProductId = productId;
                if (!products.containsKey(productId)){
                    totalProducts++;
                    products.put(productId, totalProducts);

                }

            }else if (currentLine.startsWith(userLabel)){
                String userId = currentLine.replace(userLabel, "");
                lastUserId = userId;
                if (!users.containsKey(userId)){
                    totalUsers++;
                    users.put(userId, totalUsers);
                }

            }else if (currentLine.startsWith(reviewLabel)){
                String score = currentLine.replace(reviewLabel, "");
                totalReviews++;
                bufferedWriter.write(users.get(lastUserId) + "," + products.get(lastProductId) + "," + score + "\n");
                lastUserId = "";
                lastProductId = "";
            }
        }

        bufferedReader.close();
        bufferedWriter.close();
        this.currentFileWriter.close();
    }
    public BufferedWriter createFile (String filePath) throws IOException {
        this.currentFileWriter = new FileWriter(filePath);
        BufferedWriter bufferedWriter = new BufferedWriter(this.currentFileWriter);

        return bufferedWriter;
    }


    public int getTotalReviews() {
        return totalReviews;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public List<String> getRecommendationsForUser(String userID) throws IOException, NullPointerException, TasteException {
        List<String> resultsList = new ArrayList<>();

        Integer userIntId = this.users.get(userID);
        DataModel model = new FileDataModel(new File("datasetMovies.csv"));

        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        List<RecommendedItem> recommendations = recommender.recommend(userIntId, 3);
        for (RecommendedItem recommendation : recommendations){
            for (String key : this.products.keySet()) {
                if (this.products.get(key)==recommendation.getItemID()) {
                    resultsList.add(key);
                }
            }
        }


        return resultsList;
    }


}

