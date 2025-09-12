package io.github.alathra.alathranwars.gui.customization;

import io.github.milkdrinkers.crate.internal.serialize.CrateSerializable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Describes and item in a gui
 */
public class GuiButton implements CrateSerializable<GuiButton> {
    private final GuiPosition position;
    private final Material material;
    private final ItemStack item;

    private GuiButton(GuiPosition position, Material material) {
        this(position, new ItemStack(material));
    }

    private GuiButton(GuiPosition position, ItemStack item) {
        this.position = position;
        this.material = item.getType();
        this.item = item;
    }

    /**
     * Get a new gui item with the following coordinates
     * @param position position of the item
     * @param material the base item material to use
     * @return gui item
     */
    public static GuiButton of(GuiPosition position, Material material) {
        return new GuiButton(position, material);
    }

    /**
     * Get a new gui item with the following coordinates
     * @param position position of the item
     * @param item the base item to use
     * @return gui item
     */
    public static GuiButton of(GuiPosition position, ItemStack item) {
        return new GuiButton(position, item);
    }

    /**
     * Returns the gui position object for this item
     * @return gui position
     */
    public GuiPosition getPosition() {
        return position;
    }

    /**
     * Returns the material of this item
     * @return material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Returns a clone of the item stack
     * @return item stack
     */
    public ItemStack getItem() {
        return item.clone();
    }

    @SuppressWarnings("unchecked")
    @Override
    public GuiButton deserialize(@NotNull Object uncastData, String s) throws ClassCastException {
        final Map<String, Object> data = (Map<String, Object>) uncastData;

        final Map<String, Object> positionData = (Map<String, Object>) data.get("position");
        final int col = (int) positionData.get("column");
        final int row = (int) positionData.get("row");
        final GuiPosition position = GuiPosition.of(col, row);

        final String material = (String) data.get("material");
        final Material mat = Material.getMaterial(material);
        if (mat == null)
            throw new ClassCastException("Invalid material name: %s!".formatted(material));

        return of(position, mat);
    }

    @Override
    public Object serialize(@NotNull GuiButton item) throws ClassCastException {
        final Map<String, Object> data = new LinkedHashMap<>();

        final Map<String, Object> positionData = new LinkedHashMap<>();
        positionData.put("column", item.getPosition().col());
        positionData.put("row", item.getPosition().row());
        data.put("position", positionData);

        data.put("material", item.getMaterial().toString());
        return data;
    }

    @Override
    public Class<GuiButton> getClazz() {
        return GuiButton.class;
    }
}
