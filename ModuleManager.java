package dev.vapemace.client.module;

import dev.vapemace.client.module.impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        modules.add(new SilentAimModule());
        modules.add(new AutoStunSlamModule());
        modules.add(new AutoBreachSwapModule());
        modules.add(new AutoMaceModule());
        modules.add(new AutoPearlCatchModule());
        modules.add(new WTapModule());
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz) {
        return (T) modules.stream()
                .filter(m -> m.getClass().equals(clazz))
                .findFirst()
                .orElse(null);
    }
}
