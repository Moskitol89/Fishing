import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.helpers.ItemProcessing;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;

@ScriptManifest(name = "Fishing", description = "Fishing + cooking", author = "Moskitol89",
        version = 0.1, category = Category.FISHING, image = "")
public class Fishing extends AbstractScript {

    private final Player PLAYER = Players.getLocal();
    private Area shrimpArea = new Area(3234, 3156, 3245, 3142);
    private Area cookHouse =  new Area(3232, 3197, 3236, 3195);
//    private Area cookHouse = new Area(3233, 3199, 3236, 3199);
    private boolean comingToCook = false;
    private boolean comingToFish = false;
    private enum STATES {
        FISHING, COOKING
    }
    private STATES state = STATES.FISHING;
    @Override
    public int onLoop() {
        switch (state) {
            case FISHING -> {
                log("Fishing");
                if(Inventory.isFull()) {
//                    Inventory.dropAll("Raw shrimps");
                    state = STATES.COOKING;
                    comingToCook = true;
                    break;
                }
                while (comingToFish) {
                    Walking.walk(shrimpArea.getRandomTile());
                    if(shrimpArea.contains(PLAYER.getTile())) {
                        comingToFish = false;
                    }
                }
                    if(!PLAYER.isMoving() && !PLAYER.isAnimating()) {
                        NPCs.closest("Fishing spot").interact("Net");
                    }
            }
            case COOKING -> {
                if(!Inventory.contains("Raw shrimps")) {
                    Inventory.dropAll("Shrimps","Burnt shrimp");
                    state = STATES.FISHING;
                    comingToFish = true;
                    break;
                }
                if(comingToCook) {
                    Walking.walk(cookHouse.getRandomTile());
                    if(cookHouse.contains(PLAYER.getTile())) {
                        comingToCook = false;
                    }
                }
                GameObject cookRange = GameObjects.closest("Range");
//                if(!cookRange.canReach()) {
//                    GameObjects.closest("Door").interact("Open");
//                    sleep(Calculations.random(900,1600));
//                }
                if(!PLAYER.isAnimating() && !PLAYER.isMoving()) {
                    cookRange.interact("Cook");
                    if(ItemProcessing.isOpen()) {
                        ItemProcessing.makeAll("Raw shrimps");
                    }
                }
            }
        }

        return Calculations.random(900, 2500);
    }
}
