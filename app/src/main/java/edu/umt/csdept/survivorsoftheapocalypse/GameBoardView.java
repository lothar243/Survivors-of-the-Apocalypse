package edu.umt.csdept.survivorsoftheapocalypse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.Calendar;

/**
 * The main surfaceview for the game
 */
public class GameBoardView extends SurfaceView implements SurfaceHolder.Callback {
    final String NAME = "MainSurfaceView";

    private MainThread thread;

    public GameBoardView(Context context) {
        super(context);
    }

    public GameBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GameBoard gameBoard;
    private GameState gameState;

    Matrix canvasTransformationMatrix;
    float[] inititialTouchPosition;
    float[] lastTouchPositions;
    boolean moving = false;
    boolean hasLongPressed = false;
    final float DISTANCE_SQUARED_BEFORE_MOVING = 100;
    long timeOfInitialPress; // used for detecting a long onPress

    public GameBoardView(Context context, GameState gameState) {
        super(context);
        this.gameState = gameState;
        gameBoard = new GameBoard(getResources(), this.gameState);

        // add the game loop
        getHolder().addCallback(this);


        // make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        // we can safely start the game loop
        thread = new MainThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();

        resetMapTranslations();
    }

    //initializing the canvas transformation matrix so that the 0,0 hex is in the lower left, 4,4 is upper right
    public void resetMapTranslations() {
        canvasTransformationMatrix = new Matrix();
        PointF rectCoordsOfHex = Hex.hexCoordsToRect(4, 4);
        float[] pointSources = {0, 0, rectCoordsOfHex.x, rectCoordsOfHex.y};
        float[] pointDestinations = {0, getHeight(), getWidth(), 0};
        canvasTransformationMatrix.setPolyToPoly(pointSources, 0, pointDestinations, 0, 2);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // stop the thread when it's not displayed
        boolean retry = true;
        while(retry) {
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                Log.d(NAME, "ACTION_DOWN");
                inititialTouchPosition = new float[2];
                lastTouchPositions = new float[2];
                inititialTouchPosition[0] = lastTouchPositions[0] = event.getX();
                inititialTouchPosition[1] = lastTouchPositions[1] = event.getY();
                moving = false;
                hasLongPressed = false;
                timeOfInitialPress = Calendar.getInstance().getTimeInMillis();
                break;
            case MotionEvent.ACTION_UP:
//                Log.d(NAME, "ACTION_UP");
                if(!moving)
                    onPress();
                // forget the the previous touch
                inititialTouchPosition = null;
                lastTouchPositions = null;
                break;
            case MotionEvent.ACTION_MOVE:
                // allow zoom/translate/rotate, but not skew, so only paying attention to the first two touch points
                int numTouchPoints = Math.min(2, event.getPointerCount());

                float[] currentTouchPositions = new float[numTouchPoints * 2];
                for(int i = 0; i < currentTouchPositions.length / 2; i++) {
                    currentTouchPositions[2 * i] = event.getX(i);
                    currentTouchPositions[2 * i + 1] = event.getY(i);
                }
                if(currentTouchPositions.length != lastTouchPositions.length) {
                    // a new touch point has been added or removed, so not moving the screen
                    // do nothing
//                    Log.d(NAME, "pointerCount changed");
                }
                else {
                    Matrix transformationMatrix = new Matrix();
                    transformationMatrix.setPolyToPoly(lastTouchPositions, 0, currentTouchPositions, 0, numTouchPoints);
                    // alter the canvas so that the last touch points are now in the position of the new touch points
                    canvasTransformationMatrix.postConcat(transformationMatrix);
                    // if the screen isn't already classified as moving, determine if it should be
                    if (!moving && (numTouchPoints > 1 ||
                            distanceSquared(
                                    currentTouchPositions[0], currentTouchPositions[1],
                                    inititialTouchPosition[0], inititialTouchPosition[1]
                            ) > DISTANCE_SQUARED_BEFORE_MOVING)) {
                        moving = true;
                    }
//                    Log.d(NAME, "moving screen");
                }
                // getting ready for the next iteration
                lastTouchPositions = currentTouchPositions;
                break;
        }
        return true;
    }

    public float distanceSquared(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    public void onPress() {
        // determine the touchPoint location before the canvas translations
        Matrix inverseMatrix = new Matrix();
        if(!canvasTransformationMatrix.invert(inverseMatrix)) {
            // something odd happened - the matrix should be invertible
            resetMapTranslations();
            return;
        }
        if(lastTouchPositions == null) {
            Log.e(NAME, "onPress() happened with a null location");
            return;
        }
        inverseMatrix.mapPoints(lastTouchPositions);
        Point clickedIndex = Hex.getHexIndex(lastTouchPositions[0], lastTouchPositions[1]);
//        Hex.rectCoordsToHex(imageTouchPoints[0], imageTouchPoints[1]);
        Log.d(NAME, "onPress at " + clickedIndex.x + ", " + clickedIndex.y);
    }

    public void update() {
        gameBoard.update();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.setMatrix(canvasTransformationMatrix);
        canvas.save();
        super.draw(canvas);
        gameBoard.draw(canvas);
    }
}
