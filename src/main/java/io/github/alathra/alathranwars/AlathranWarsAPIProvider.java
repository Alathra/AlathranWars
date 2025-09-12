package io.github.alathra.alathranwars;

import io.github.alathra.alathranwars.api.AlathranWarsAPI;

class AlathranWarsAPIProvider extends AlathranWarsAPI implements Reloadable {
    @SuppressWarnings("unused")
    private final AlathranWars instance;

    public AlathranWarsAPIProvider(AlathranWars instance) {
        this.instance = instance;
        AlathranWarsAPI.setInstance(this);
    }
}
