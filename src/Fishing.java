import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
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
    private Area cookHouse = new Area(3230, 3198, 3237, 3195);

    private Area sellingArea = new Area(3208, 3249, 3214, 3244);
//    private Area cookHouse = new Area(3233, 3199, 3236, 3199);
    private boolean comingToCook = false;
    private boolean comingToFish = true;
    private boolean comingToSell = false;
    private enum STATES {
        FISHING, COOKING, SELLING
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
                    comingToFish = false;
                    break;
                }
                while (comingToFish) {
                    Walking.walk(shrimpArea.getRandomTile());
                    if(shrimpArea.contains(PLAYER.getTile())) {
                        comingToFish = false;
                    }
                    sleep(1000L);
                }
                    if(!PLAYER.isMoving() && !PLAYER.isAnimating()) {
                        NPCs.closest("Fishing spot").interact("Net");
                    }
            }
            case COOKING -> {
                if(!Inventory.contains("Raw shrimps","Raw anchovies")) {
                    Inventory.dropAll("Burnt shrimp","Burnt anchovies","Burnt fish");
                    state = STATES.SELLING;
                    comingToSell = true;
                    break;
                }
                while(comingToCook) {
                    Walking.walk(cookHouse.getRandomTile());
                    if(cookHouse.contains(PLAYER.getTile())) {
                        comingToCook = false;
                    }
                    sleep(1000L);
                }
                GameObject cookRange = GameObjects.closest("Range");
//                if(!cookRange.canReach()) {
//                    GameObjects.closest("Door").interact("Open");
//                    sleep(Calculations.random(900,1600));
//                }
                if(!PLAYER.isAnimating() && !PLAYER.isMoving()) {
                    if(Inventory.contains("Raw shrimps")) {
                        Inventory.get("Raw shrimps").useOn(cookRange);
                        if(ItemProcessing.isOpen()) {
                            ItemProcessing.makeAll("Raw shrimps");
                        }
                        sleep(1000L);
                    } else if(Inventory.contains("Raw anchovies")) {
                        Inventory.get("Raw anchovies").useOn(cookRange);
                        if (ItemProcessing.isOpen()) {
                            ItemProcessing.makeAll("Raw anchovies");
                        }
                        sleep(1000L);
                    }
                }
            }
            case SELLING -> {
                while(comingToSell) {
                    Walking.walk(sellingArea.getRandomTile());
                    if(sellingArea.contains(PLAYER.getTile())) {
                        comingToSell = false;
                    }
                    sleep(1000L);
                }
                Shop.open();
                if (Shop.isOpen()) {
                    Shop.sellFifty("Shrimps");
                    Shop.sellFifty("Anchovies");
                }
                state = STATES.FISHING;
                comingToFish = true;
                break;
            }
        }

        return Calculations.random(900, 1500);
    }
}
