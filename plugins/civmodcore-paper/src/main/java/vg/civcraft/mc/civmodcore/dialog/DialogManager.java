package vg.civcraft.mc.civmodcore.dialog;

import com.google.common.hash.Hashing;
import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.player.PaperPlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;

@SuppressWarnings("UnstableApiUsage")
public final class DialogManager implements Listener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DialogManager.class);
    private static final String DIALOG_ID_KEY = "dialogId";
    private static final Key CONFIRM_DIALOG_KEY = Key.key("civmodcore", "confirm_dialog");
    private static final Key CLOSE_DIALOG_KEY = Key.key("civmodcore", "close_dialog");

    private record InternalDialogCallback(float dialogId, DialogCallback handler) {}
    private static final Map<UUID, InternalDialogCallback> callbacks = new ConcurrentHashMap<>();

    public static void showDialog(
        final @NotNull Player player,
        final @NotNull Component title,
        final @NotNull List<? extends @NotNull DialogBody> body,
        final @NotNull List<? extends @NotNull DialogInput> inputs,
        final @NotNull DialogCallback callback
    ) {
        final float dialogId = ThreadLocalRandom.current().nextFloat();
        final InternalDialogCallback previousCallback = callbacks.put(player.getUniqueId(), new InternalDialogCallback(
            dialogId,
            Objects.requireNonNull(callback)
        ));
        if (previousCallback != null) {
            LOGGER.warn(
                "Replacing player[{}]'s dialog with {}: {} with: {}",
                player.getName(),
                DIALOG_ID_KEY,
                previousCallback.dialogId(),
                dialogId
            );
        }
        player.showDialog(Dialog.create((b) -> b
            .empty()
            .base(
                DialogBase.builder(Objects.requireNonNull(title))
                    .canCloseWithEscape(true)
                    .pause(false)
                    .afterAction(DialogBase.DialogAfterAction.NONE)
                    .body(List.copyOf(body))
                    .inputs(List.copyOf(inputs))
                    .build()
            )
            .type(DialogType.confirmation(
                ActionButton.create(
                    Component.text("Confirm", NamedTextColor.GREEN),
                    null,
                    100,
                    DialogHelpers.customClick(CONFIRM_DIALOG_KEY, (nbt) -> nbt.setFloat(DIALOG_ID_KEY, dialogId))
                ),
                ActionButton.create(
                    Component.translatable("gui.cancel", NamedTextColor.RED),
                    null,
                    100,
                    DialogAction.customClick(CLOSE_DIALOG_KEY, null)
                )
            ))
        ));
    }

    public static void clearCallbacks() {
        callbacks.clear();
    }

    @EventHandler
    private static void handleCustomAction(
        final @NotNull PaperPlayerCustomClickEvent event
    ) {
        // Getting the player as recommended by https://docs.papermc.io/paper/dev/dialogs/#reading-the-input
        final Player player; switch (event.getCommonConnection()) {
            case final PlayerGameConnection conn: player = conn.getPlayer(); break;
            default: return;
        }
        final Key clickAction = event.getIdentifier();
        if (CLOSE_DIALOG_KEY.equals(clickAction)) {
            player.closeDialog();
            return;
        }
        if (!CONFIRM_DIALOG_KEY.equals(clickAction)) {
            return;
        }
        final DialogResponseView view = event.getDialogResponseView();
        if (view == null) {
            LOGGER.warn(
                "Player[{}] sent a dialog-submit without any data?!",
                player.getName()
            );
            return;
        }
        final Float responseId = view.getFloat(DIALOG_ID_KEY);
        if (responseId == null) {
            LOGGER.warn(
                "Player[{}] sent a dialog-submit without a {}?! Payload: {}",
                player.getName(),
                DIALOG_ID_KEY,
                view.payload()
            );
            return;
        }
        final InternalDialogCallback callback = callbacks.remove(player.getUniqueId());
        if (callback == null) {
            LOGGER.warn(
                "Player[{}] sent a non-prompted dialog submit for {}: {}?!",
                player.getName(),
                DIALOG_ID_KEY,
                responseId
            );
            return;
        }
        if (callback.dialogId() != responseId) {
            LOGGER.warn(
                "Player[{}] sent a dialog-submit with for {}: {}, expected: {}!",
                player.getName(),
                DIALOG_ID_KEY,
                responseId,
                callback.dialogId()
            );
            return;
        }
        player.closeDialog();
        try {
            callback.handler().handle(view);
        }
        catch (final Exception e) {
            final String errorId = "error:%s:%s".formatted(
                player.getName(),
                HexFormat.of().formatHex(
                    Hashing.sha1()
                        .newHasher()
                        .putLong(System.currentTimeMillis())
                        .putInt(ThreadLocalRandom.current().nextInt())
                        .hash()
                        .asBytes()
                )
            );
            LOGGER.error(
                "Player[{}]'s dialog submit [{}: {}] failed during handling! Errno: {}",
                event.getIdentifier(),
                DIALOG_ID_KEY,
                responseId,
                errorId,
                e
            );
            player.sendMessage(
                Component.text()
                    .color(NamedTextColor.RED)
                    .append(
                        Component.text("Something went wrong handling that Dialog action! Please give \""),
                        ChatUtils.clickToCopy(errorId),
                        Component.text("\" to the admins!")
                    )
                    .build()
            );
            return;
        }
    }

    @EventHandler
    private static void expireCallbackOnLogout(
        final @NotNull PlayerQuitEvent event
    ) {
       callbacks.remove(event.getPlayer().getUniqueId());
    }

    private DialogManager() {}
    private static final DialogManager INSTANCE = new DialogManager();
    /// Should only be called by [CivModCorePlugin#onEnable()]
    @ApiStatus.Internal
    public static void init(
        final @NotNull CivModCorePlugin plugin
    ) {
        plugin.registerListener(INSTANCE);
    }
}
