package edu.umt.csdept.survivorsoftheapocalypse;

/**
 * Created by sinless on 3/4/16.
 */
public class PlayCardFactory {
    private static PlayCardFactory ourInstance = new PlayCardFactory();

    public static PlayCardFactory getInstance() {
        return ourInstance;
    }

    private PlayCardFactory() {
    }

    public PlayerCard makeCard(String cardName){
        PlayerCard playerCard = null;
        if (cardName.equalsIgnoreCase("Bumper Crop")) {
            playerCard = new BumperCrop();
        }
        else if (cardName.equalsIgnoreCase("New Growth")) {
            playerCard = new NewGrowth();
        }
        else if (cardName.equalsIgnoreCase("Hidden Stores")) {
            playerCard = new HiddenStores();
        }
        return playerCard;
    }
}
