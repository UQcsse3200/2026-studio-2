package com.csse3200.game.areas;

import static com.csse3200.game.entities.factories.ObstacleFactory.LEVEL_END_TEXTURE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.level.LevelTriggerComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.CaveEntranceRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Simulates the player moving around the Level 2 exit with real Box2D physics, checking that only
 * entering the door itself requests Level 3.
 */
@ExtendWith(GameExtension.class)
class Level2ExitDoorTest {
  private static final float STEP = 0.016f;
  // Matches PlayerFactory: 26 x 82 default frame, scaled to 0.6 wide.
  private static final float PLAYER_WIDTH = 0.6f;
  private static final float PLAYER_HEIGHT = 0.6f * 82f / 26f;

  private PhysicsService physics;
  private EntityService entityService;
  private Level2GameArea area;
  private Entity door;
  private Entity player;
  private Body playerBody;
  private final List<String> requestedLevels = new ArrayList<>();

  @BeforeEach
  void setUp() throws Exception {
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(STEP);
    ServiceLocator.registerTimeSource(time);
    physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
    entityService = new EntityService();
    ServiceLocator.registerEntityService(entityService);
    ServiceLocator.registerRenderService(mock(RenderService.class));

    InputService inputService = mock(InputService.class);
    ServiceLocator.registerInputService(inputService);
    ResourceService resourceService = mock(ResourceService.class);
    ServiceLocator.registerResourceService(resourceService);
    Texture doorTexture = mock(Texture.class);
    when(doorTexture.getWidth()).thenReturn(1536);
    when(doorTexture.getHeight()).thenReturn(1024);
    when(resourceService.getAsset(CaveEntranceRenderComponent.TEXTURE, Texture.class))
        .thenReturn(doorTexture);

    when(resourceService.getAsset(LEVEL_END_TEXTURE, Texture.class)).thenReturn(doorTexture);

    // Level 2 terrain uses one world unit per tile.
    TerrainComponent terrain = mock(TerrainComponent.class);
    when(terrain.tileToWorldPosition(any(GridPoint2.class)))
        .thenAnswer(
            inv -> {
              GridPoint2 tile = inv.getArgument(0);
              return new Vector2((float) tile.x, (float) tile.y);
            });

    area =
        new Level2GameArea(mock(TerrainFactory.class), mock(CameraComponent.class), new Entity());
    area.terrain = terrain;
    Method spawnExitDoor = Level2GameArea.class.getDeclaredMethod("spawnExitDoor");
    spawnExitDoor.setAccessible(true);
    spawnExitDoor.invoke(area);

    door = area.getLevelChanger();
    door.getEvents().addListener("triggerNextLevel", (String level) -> requestedLevels.add(level));

    // Solid terrain around the exit, as laid out in Level2Config.
    solidBox(33, 7, 2, 1); // P11, the ledge the door stands on
    solidBox(32, 7, 1, 9); // G14, wall left of the door
    solidBox(38, 7, 1, 1); // P10, the step before the door
    solidBox(33, 14, 9, 2); // G13, ceiling above the door

    player =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.PLAYER))
            .addComponent(new PlayerActions());
    player.setScale(PLAYER_WIDTH, PLAYER_HEIGHT);
    player
        .getComponent(ColliderComponent.class)
        .setAsBoxAligned(
            player.getScale(), PhysicsComponent.AlignX.CENTER, PhysicsComponent.AlignY.BOTTOM);
    // Only the physics body is needed; the player's other systems are out of scope here.
    player.getComponent(PhysicsComponent.class).create();
    player.getComponent(ColliderComponent.class).create();
    playerBody = player.getComponent(PhysicsComponent.class).getBody();
    playerBody.setGravityScale(0f);
  }

  @AfterEach
  void tearDown() {
    entityService.dispose();
    physics.getPhysics().dispose();
    ServiceLocator.clear();
  }

  @Test
  void doorIsTheLevelChangerAndTargetsLevel3() {
    assertNotNull(door);
    assertNotNull(door.getComponent(CaveEntranceRenderComponent.class));
    assertNotNull(door.getComponent(LevelTriggerComponent.class));
    assertTrue(door.getComponent(ColliderComponent.class).getFixture().isSensor());
    assertEquals(new Vector2(33f, 8f), door.getPosition());
    assertEquals(new Vector2(2f, 2.67f), door.getScale());

    long triggers =
        area.areaEntities.stream()
            .filter(e -> e.getComponent(LevelTriggerComponent.class) != null)
            .count();
    assertEquals(1, triggers, "the door must be Level 2's only next-level trigger");
  }

  @Test
  void walkingBeneathTheLedgeDoesNotLoadLevel3() {
    // Feet low enough for the head to clear P11 and G14.
    move(new Vector2(28f, 7f - PLAYER_HEIGHT - 0.1f), new Vector2(4f, 0f), 180);

    assertTrue(playerBody.getPosition().x > 37f, "player should have passed under the ledge");
    assertTrue(requestedLevels.isEmpty());
  }

  @Test
  void jumpingIntoTheLedgeUndersideDoesNotLoadLevel3() {
    // Directly under the doorway, pushing up until the head is stopped by P11's underside.
    move(new Vector2(34f, 7f - PLAYER_HEIGHT - 1f), new Vector2(0f, 8f), 60);

    float head = playerBody.getPosition().y + PLAYER_HEIGHT;
    assertEquals(7f, head, 0.05f, "player's head should be pressed against P11");
    assertTrue(requestedLevels.isEmpty());
  }

  @Test
  void fallingPastTheLedgeDoesNotLoadLevel3() {
    // Down the gap between P11 and P10, right beside the door.
    move(new Vector2(36.5f, 11.5f), new Vector2(0f, -10f), 120);

    assertTrue(playerBody.getPosition().y < 0f, "player should have fallen below the ledge");
    assertTrue(requestedLevels.isEmpty());
  }

  @Test
  void jumpingOverTheDoorDoesNotLoadLevel3() {
    // Across the top of the door frame, feet above the doorway, from P10 towards the wall.
    move(new Vector2(38.5f, 10.8f), new Vector2(-4f, 0f), 120);

    assertTrue(playerBody.getPosition().x < 34f, "player should have passed over the door");
    assertTrue(requestedLevels.isEmpty());
  }

  @Test
  void jumpingBesideTheDoorDoesNotLoadLevel3() {
    // Straight up and down from P10, next to the door but never in the doorway.
    move(new Vector2(38.5f, 8f), new Vector2(0f, 6f), 40);
    move(playerBody.getPosition().cpy(), new Vector2(0f, -6f), 40);

    assertTrue(requestedLevels.isEmpty());
  }

  /** Places the player's bottom-left corner at start and drives it at velocity for steps. */
  private void move(Vector2 start, Vector2 velocity, int steps) {
    playerBody.setTransform(start, 0f);
    World world = physics.getPhysics().getWorld();
    for (int i = 0; i < steps; i++) {
      playerBody.setLinearVelocity(velocity);
      world.step(STEP, 6, 2);
    }
  }

  private void solidBox(float x, float y, float width, float height) {
    BodyDef def = new BodyDef();
    def.position.set(x + width / 2f, y + height / 2f);
    Body body = physics.getPhysics().getWorld().createBody(def);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(width / 2f, height / 2f);
    Fixture fixture = body.createFixture(shape, 0f);
    shape.dispose();
    var filter = fixture.getFilterData();
    filter.categoryBits = PhysicsLayer.GROUND;
    fixture.setFilterData(filter);
  }
}
