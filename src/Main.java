import models.Game;
import models.Player;
import models.PlayerType;

public class Main {

    public static void main(String[] args) {
        RandomPlayer whitePlayer = new RandomPlayer(PlayerType.white);
        AlphaBetaAi blackPlayer = new AlphaBetaAi(PlayerType.black,2);
        Game game = new Game(whitePlayer, blackPlayer);
        Player player = game.play();
        System.out.println(player.getType());
    }

}
