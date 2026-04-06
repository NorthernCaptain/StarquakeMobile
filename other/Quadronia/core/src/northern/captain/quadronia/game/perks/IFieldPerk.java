package northern.captain.quadronia.game.perks;

import northern.captain.quadronia.game.Cell;
import northern.captain.quadronia.game.Engine;
import northern.captain.quadronia.game.Field;

/**
 * Created by leo on 04.04.15.
 */
public interface IFieldPerk
{
    int PERK_BARRIER = 1;
    int PERK_GRAVITY_TOWER = 2;
    int PERK_NAIL = 3;
    int PERK_EXIT = 4;

    int BONUS_START_ID = 100;
    int BONUS_ANTI_BARRIER = 100;
    int BONUS_SCORE_X2 = 101;
    int BONUS_BACKSTEP = 102;
    int BONUS_SWAP = 103;

    /**
     * Find a place where this perk can be set to the field
     * @param field
     * @return true if the perk was put to the field, false otherwise
     */
    boolean putToField(Engine game, Field field);

    /**
     * Remove the perk from the field
     * @param game
     * @return true if removed, false otherwise
     */
    boolean removeFromField(Engine game);

    /**
     * Gets the type of the perk
     * @return type of the perk, see contants in IFieldPerk
     */
    int getType();

    /**
     * Apply this bonus to the game
     * @param game
     * @return
     */
    boolean applyBonus(Engine game, Cell appliedCell);

    boolean applyOnSet(Engine game, Cell cell);
}
