package edu.umt.csdept.survivorsoftheapocalypse;

import android.graphics.Bitmap;

/**
 * Created by sinless on 2/25/16.
 */
public class Tile implements Card{
    private String title;
    private String resource;
    private int banditDanger;
    private int zombieDanger;


    @Override
    public void onAquire(GameState gameState) {
    onPlay(gameState);
    }

    @Override
    public void onPlay(GameState gameState) {
    //do what needs to be done to add tile to board
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public int getBanditDanger() {
        return banditDanger;
    }

    public void setBanditDanger(int banditDanger) {
        this.banditDanger = banditDanger;
    }

    public int getZombieDanger() {
        return zombieDanger;
    }

    public void setZombieDanger(int zombieDanger) {
        this.zombieDanger = zombieDanger;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Tile{" +
                "title='" + title + '\'' +
                ", resource='" + resource + '\'' +
                ", banditDanger=" + banditDanger +
                ", zombieDanger=" + zombieDanger +
                '}';
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
