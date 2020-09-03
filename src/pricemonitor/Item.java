/**
 * 
 */
package pricemonitor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Creates a new item object to display
 * @author Chris Boado 905714629
 * @version Mar 21, 2016
 */
public class Item {
    private String name;
    private String priceString;
    private Timestamp lastUpdate;
    private ArrayList<Integer> priceList;
    private int ID;
    private int highest;
    private int lowest;
    private int average;
    private int vendorPrice;
    private int diffHours;
    private double uncertainty;
    private double median;
    private double std;
    private double actualProfit;
    private double percent5Profit;
    private double percent10Profit;
    private double percent15Profit;
    private Recipe recipe;
    
    /**
     * Constructs a new item
     * @param name
     * @param priceList
     * @param vendorPrice
     * @param recipe
     * @param lastUpdate
     * @throws Exception 
     */
    public Item(int ID, String name, ArrayList<Integer> priceList, int vendorPrice, 
            String[] recipeArray, int totalPrice, Timestamp lastUpdate) throws Exception {
        this.setID(ID);
        this.setName(name);
        this.setPriceList(priceList);
        this.setVendorPrice(vendorPrice);
        this.setRecipe(recipeArray, totalPrice);
        this.setLastUpdate(lastUpdate);
        findHighestAndLowestAndAverage();
        calculateUncertainty();
        findMedian();
        calculateStd();
        calculateLastUpdate();
        calculateProfit();
    }
    
    /**
     * Adds a new price to the list and array
     * @param newPrice
     */
    public void addNewPrice(int newPrice) {
        priceList.add(newPrice);
        calculateNewHighest();
        calculateNewLowest();
        calculateNewAverage();
    }
    
    /**
     * Removes the last price added form the list and array
     */
    public void removeLastPrice() {
        int removed = priceList.get(priceList.size() - 1);
        priceList.remove(priceList.size() - 1);
        calculateNewHighest(removed);
        calculateNewLowest(removed);
        calculateNewAverage();
    }
    
    /**
     * Calculates highest price for add method
     */
    private void calculateNewHighest() {
        if (priceList.get(0) > highest) {
            setHighest(priceList.get(0));
        }
    }
    
    /**
     * Calculates highest price for remove method
     * @param removed
     */
    private void calculateNewHighest(int removed) {
        if (!priceList.contains(removed)) {
            int max = priceList.get(0);
            for (int i = 1; i < priceList.size(); i++) {
                if (priceList.get(i) > max) {
                    max = priceList.get(i);
                }
            }
            setHighest(max);
        }
    }
    
    /**
     * Calculates lowest price for add method
     */
    private void calculateNewLowest() {
        if (priceList.get(0) < lowest) {
            setLowest(priceList.get(0));
        }
    }
    
    /**
     * Calculates lowest Price for remove method
     * @param removed
     */
    private void calculateNewLowest(int removed) {
        if (!priceList.contains(removed)) {
            int min = priceList.get(0);
            for (int i = 1; i < priceList.size(); i++) {
                if (priceList.get(i) < min) {
                    min = priceList.get(i);
                }
            }
            setLowest(min);
        }
    }
    
    /**
     * Calculates new new average price
     */
    private void calculateNewAverage() {
        int temp = 0;
        for (int i = 0; i < priceList.size(); i++) {
            temp = temp + priceList.get(0);
        }
        setAverage(temp / priceList.size());
    }
    
    private void findHighestAndLowestAndAverage() {
        average = 0;
        highest = lowest = priceList.get(0);
        for (int i = 0; i < priceList.size(); i++) {
            if (priceList.get(i) > highest) {
                highest = priceList.get(i);
            }
            if (priceList.get(i) < lowest) {
                lowest = priceList.get(i);
            }
            average = average + priceList.get(i);
        }
        average = average / priceList.size();
    }
    
    /**
     * Calculates time since last update
     */
    private void calculateLastUpdate() {
        Date date = new Date();
        Timestamp currentTime = new Timestamp(date.getTime());
        long diff = getLastUpdate().getTime() - currentTime.getTime();
        setDiffHours((int) (diff / (-60 * 60 * 1000)));
    }
    
    /**
     * Calculates uncertainty of current price
     */
    private void calculateUncertainty() {
        double range = highest - lowest;
        setUncertainty(Math.ceil((range / (2 * Math.sqrt(priceList.size()))) * 100) / 100);
    }
    
    /**
     * Finds the median value of all prices
     */
    @SuppressWarnings("unchecked")
    private void findMedian() {
        ArrayList<Integer> temp = (ArrayList<Integer>) priceList.clone();
        Collections.sort(temp);
        if (priceList.size() % 2 == 1) {
            setMedian(temp.get(temp.size() / 2));
        }
        if (priceList.size() % 2 == 0) {
            setMedian(((double) temp.get(temp.size() / 2) +  (double) temp.get(((temp.size() / 2) - 1))) / 2);
        }
    }
    
    /**
     * Calculates the standard deviation of entire pricelist
     */
    private void calculateStd() {
        double variance = 0;
        for (double i: priceList) {
            variance = variance + Math.pow((i - average), 2);
        }
        variance = variance / priceList.size();
        setStd(Math.ceil(Math.sqrt(variance) * 100) / 100);
    }
    
    /**
     * Calculates the profit based on the selling price and the recipe price
     * with 5% commission
     */
    private void calculateProfit() {
        if (this.getRecipe().getTotalPrice() == 0) {
            setActualProfit(Math.ceil(((this.getPriceList().get(this.getPriceList().size() - 1) - this.getPriceList().get(this.getPriceList().size() - 1) * 0.05 - this.getAverage()) / this.getAverage() * 100) * 100) / 100);
            setPercent5Profit(Math.ceil(((this.getAverage() * 1.05) / 0.95) * 100) / 100);
            setPercent10Profit(Math.ceil(((this.getAverage() * 1.1) / 0.95) * 100) / 100);
            setPercent15Profit(Math.ceil(((this.getAverage() * 1.15) / 0.95) * 100) / 100);
        }
        else {
            setActualProfit(Math.ceil(((this.getPriceList().get(this.getPriceList().size() - 1) - this.getPriceList().get(this.getPriceList().size() - 1) * 0.05 - this.getAverage()) / this.getRecipe().getTotalPrice()) * 100) / 100);
            setPercent5Profit(Math.ceil(((this.getRecipe().getTotalPrice() * 1.05) / 0.95) * 100) / 100);
            setPercent10Profit(Math.ceil(((this.getRecipe().getTotalPrice() * 1.1) / 0.95) * 100) / 100);
            setPercent15Profit(Math.ceil(((this.getRecipe().getTotalPrice() * 1.15) / 0.95) * 100) / 100);
        }
    }
    
    public int getID() {
        return ID;
    }
    
    public void setID(int ID) {
        this.ID = ID;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Timestamp getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    
    public ArrayList<Integer> getPriceList() {
        return priceList;
    }
    
    public String priceString() {
        priceString = "";
        for (int temp : priceList) {
            priceString = priceString + String.valueOf(temp) + ",";
        }
        priceString = priceString.substring(0, priceString.length() - 1);
        return priceString;
    }
    
    public void setPriceList(ArrayList<Integer> priceList) {
        this.priceList = priceList;
    }

    public int getHighest() {
        return highest;
    }

    public void setHighest(int highest) {
        this.highest = highest;
    }

    public int getLowest() {
        return lowest;
    }

    public void setLowest(int lowest) {
        this.lowest = lowest;
    }

    public int getAverage() {
        return average;
    }

    public void setAverage(int average) {
        this.average = average;
    }
    
    public int getVendorPrice() {
        return vendorPrice;
    }
    
    public void setVendorPrice(int vendorPrice) {
        this.vendorPrice = vendorPrice;
    }
    
    public int getDiffHours() {
        return diffHours;
    }
    
    public void setDiffHours(int diffHours) {
        this.diffHours = diffHours;
    }
    
    public double getUncertainty() {
        return uncertainty;
    }
    
    public void setUncertainty(double uncertainty) {
        this.uncertainty = uncertainty;
    }
    
    public double getMedian() {
        return median;
    }
    
    public void setMedian(double median) {
        this.median = median;
    }
    
    public double getStd() {
        return std;
    }
    
    public void setStd(double std) {
        this.std = std;
    }
    
    public double getActualProfit() {
        return actualProfit;
    }
    
    public void setActualProfit(double actualProfit) {
        this.actualProfit = actualProfit;
    }
    
    public double getPercent5Profit() {
        return percent5Profit;
    }
    
    public void setPercent5Profit(double percent5Profit) {
        this.percent5Profit = percent5Profit;
    }
    
    public double getPercent10Profit() {
        return percent10Profit;
    }
    
    public void setPercent10Profit(double percent10Profit) {
        this.percent10Profit = percent10Profit;
    }
    
    public double getPercent15Profit() {
        return percent15Profit;
    }
    
    public void setPercent15Profit(double percent15Profit) {
        this.percent15Profit = percent15Profit;
    }
    
    public Recipe getRecipe() {
    	return recipe;
    }
    
    public void setRecipe(String[] recipeArray, int totalPrice) {
    	this.recipe = new Recipe(recipeArray, totalPrice);
    }
    
    /**
     * Creates a recipe class
     * @author Chris Boado 905714629
     * @version Apr 29, 2016
     */
    public class Recipe {
        private int numOfIngred;
        private String[] recipeArray;
        private int totalPrice;
        
        public Recipe(String[] recipeArray, int totalPrice) {
            setRecipeArray(recipeArray);
            setNumOfIngred();
            setTotalPrice(totalPrice);
        }
        
        public void setRecipeArray(String[] recipeArray) {
            this.recipeArray = recipeArray;
        }
        
        public String[] getRecipeArray() {
        	return recipeArray;
        }
        
        public void setNumOfIngred() {
        	numOfIngred = (recipeArray.length + 1) / 2;
        }
        
        public int getNumOfIngred() {
        	return numOfIngred;
        }
        
        public void setTotalPrice(int totalPrice) {
            this.totalPrice = totalPrice;
        }
        
        public int getTotalPrice() {
            return totalPrice;
        }
    }
}
