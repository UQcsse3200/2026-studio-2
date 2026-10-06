package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.*;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;

@ExtendWith(GameExtension.class)
class LedgeComponentTest {
    
    @Test 
    void shouldAddLedgeComponent(){
        Entity entity = new Entity().addComponent(new LedgeComponent());

        assertNotNull(entity.getComponent(LedgeComponent.class));
    }

    @Test // not that useful for testing functionality, just for coverage
    void shouldCreateLedgeComponent() {
    LedgeComponent component = new LedgeComponent();

    assertNotNull(component);
  }
}
