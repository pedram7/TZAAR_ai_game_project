import models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class AlphaBetaAi extends Player {

    private final int maxDepth;
    private int doneActions = 0;

    private static int alpha = Integer.MIN_VALUE;
    private static int beta = Integer.MAX_VALUE;

    public AlphaBetaAi(PlayerType type, int maxDepth) {
        super(type);
        this.maxDepth = maxDepth;
    }

    private int[] myStrengthBeads(Game game) {
        int[] beads = {0, 0, 0, 0, 0}; //A,B,C,Sum,Min
        Board board = game.getBoard();
        for (Board.BoardRow row : board.getRows()) {
            for (Board.BoardCell cell : row.boardCells) {
                if (cell != null && cell.bead != null && cell.bead.getPlayer().getType() == this.getType()) {
                    beads[cell.bead.getType().ordinal()] += 1;
                    beads[3] += cell.bead.getHeight();
                }
            }
        }
        beads[4] = Math.min(Math.min(beads[0], beads[1]), beads[2]);
        return beads;
    }

    private int[] opponentStrengthBeads(Game game) {
        int[] beads = {0, 0, 0, 0, Integer.MAX_VALUE}; //A,B,C,Sum,Min
        Board board = game.getBoard();
        for (Board.BoardRow row : board.getRows()) {
            for (Board.BoardCell cell : row.boardCells) {
                if (cell != null && cell.bead != null && cell.bead.getPlayer().getType() == this.getType().reverse()) {
                    beads[cell.bead.getType().ordinal()] += 1;
                    beads[3] += cell.bead.getHeight();
                }
            }
        }
        beads[4] = Math.min(Math.min(beads[0], beads[1]), beads[2]);
        return beads;
    }

    private int myAttackActionsCount(Game game) {
        List<Action> attacks = this.getAllAttacks(game.getBoard());
        return attacks.size();
    }

    private int oponnentAttackActionsCount(Game game) {
        List<Action> attacks = this.getAllOponnentAttacks(game.getBoard());
        return attacks.size();
    }


    private int eval(Game game) {
        int ans = 0;

        int[] myStrength = myStrengthBeads(game);
        int[] opStrength = opponentStrengthBeads(game);

        //strength 2
        ans += (myStrength[3] - opStrength[3]) * 40;

        //variety 5
        int var_score = 0;
        for (int i = 0; i < 3; i++) {
            var_score += myStrength[i] * 100;
            var_score -= opStrength[i] * 100;
        }
        ans += var_score;
        if (myStrength[4] == 0)
            return Integer.MIN_VALUE;
        else if (opStrength[4] == 0) {
            return Integer.MAX_VALUE;
        }else if (myStrength[4] == 1) {
            var_score-=100;
        }else if (opStrength[4] == 1) {
            var_score +=100;
        }



        //possible actions 3
        var a1 = myAttackActionsCount(game);
        var a2 = oponnentAttackActionsCount(game);
        int x = (a1-a2) * 60;
        if (a1==0)
            return Integer.MIN_VALUE;
        if(a2==0)
            return Integer.MAX_VALUE;
        if(a1==1)
            ans-=60;
        if(a2==1)
            ans+=60;

        ans += x;

        return ans;
    }


    @Override
    public Action forceAttack(Game game) {
        int maxValue = Integer.MIN_VALUE;
        alpha = Integer.MIN_VALUE;
        beta = Integer.MAX_VALUE;

        Action bestAction = null;
        ArrayList<Action> actions = getAllActions(game.getBoard());
        if (doneActions == 0 && getType() == PlayerType.white) {
            for (Action action : actions) {
                if (action.getType() == Action.ActionType.attack) {
                    Game copyGame = game.copy();
                    if (copyGame.applyActionTwo(this, action, true)) {
                        continue;
                    }
                    Player winner = copyGame.getWinner();
                    if (winner != null) {
                        if (winner.getType() == getType()) {
                            return action;
                        }
                    } else {
                        int temp = Math.max(maxValue, minForceAttack(copyGame, 0));
                        if (temp >= maxValue) {
                            maxValue = temp;
                            bestAction = action;
                        }
                    }
                }
            }
        } else {
            for (Action action : actions) {
                if (action.getType() == Action.ActionType.attack) {
                    Game copyGame = game.copy();
                    if (copyGame.applyActionTwo(this, action, true)) {
                        continue;
                    }
                    Player winner = copyGame.getWinner();
                    if (winner != null) {
                        if (winner.getType() == getType()) {
                            return action;
                        }
                    } else {
                        int temp = Math.max(maxValue, maxSecondMove(copyGame, 0));
                        if (temp >= maxValue) {
                            maxValue = temp;
                            bestAction = action;
                        }
                    }
                }
            }
        }
        doneActions++;
        return bestAction;
    }


    @Override
    public Action secondAction(Game game) {
        int maxValue = Integer.MIN_VALUE;
        alpha = Integer.MIN_VALUE;
        beta = Integer.MAX_VALUE;

        Action bestAction = null;
        ArrayList<Action> actions = getAllActions(game.getBoard());
        for (Action action : actions) {
            Game copyGame = game.copy();
            if (copyGame.applyActionTwo(this, action, false)) {
                continue;
            }
            Player winner = copyGame.getWinner();
            if (winner != null) {
                if (winner.getType() == getType()) {
                    return action;
                }
            } else {
                int temp = Math.max(maxValue, minForceAttack(copyGame, 0));
                if (temp > maxValue) {
                    maxValue = temp;
                    bestAction = action;
                }
            }
        }
        doneActions++;
        if (bestAction == null) {
            Random random = new Random();
            return actions.get(random.nextInt(actions.size()));
        }
        return bestAction;
    }

    private int maxForceAttack(Game game, int depth) {
        if (depth == maxDepth) {
            return eval(game);
        }
        int maxValue = Integer.MIN_VALUE;
        ArrayList<Action> actions = getAllActions(game.getBoard());
        for (Action action : actions) {
            if (action.getType() == Action.ActionType.attack) {
                Game copyGame = game.copy();
                if (copyGame.applyActionTwo(this, action, true)) {
                    continue;
                }
                Player winner = copyGame.getWinner();
                if (winner != null) {
                    if (winner.getType() == getType()) {
                        return Integer.MAX_VALUE;
                    }
                } else {
                    maxValue = Math.max(maxValue, maxSecondMove(copyGame, depth + 1));
                    alpha = Math.min(alpha, maxValue);
                }
                if (beta <= alpha) {
                    return alpha;
                }
            }
        }
        return maxValue;
    }

    private int maxSecondMove(Game game, int depth) {
        if (depth == maxDepth) {
            return eval(game);
        }
        int maxValue = Integer.MIN_VALUE;
        ArrayList<Action> actions = getAllActions(game.getBoard());
        for (Action action : actions) {
            Game copyGame = game.copy();
            if (copyGame.applyActionTwo(this, action, false)) {
                continue;
            }
            Player winner = copyGame.getWinner();
            if (winner != null) {
                if (winner.getType() == getType()) {
                    return Integer.MAX_VALUE;
                } else {
                    return Integer.MIN_VALUE;
                }
            } else {
                maxValue = Math.max(maxValue, minForceAttack(copyGame, depth + 1));
                alpha = Math.min(alpha, maxValue);
            }
            if (beta <= alpha) {
                return alpha;
            }
        }
        return maxValue;
    }

    private int minForceAttack(Game game, int depth) {

        if (depth == maxDepth) {
            return eval(game);
        }

        Player opp = getOpp(game);
        int minValue = Integer.MAX_VALUE;
        ArrayList<Action> actions = opp.getAllActions(game.getBoard());
        for (Action action : actions) {
            if (action.getType() == Action.ActionType.attack) {
                Game copyGame = game.copy();
                if (copyGame.applyActionTwo(opp, action, true)) {
                    continue;
                }
                Player winner = copyGame.getWinner();
                if (winner != null) {
                    if (winner.getType() == getType().reverse()) {
                        return Integer.MIN_VALUE;
                    }
                } else {
                    minValue = Math.min(minValue, minSecondMove(copyGame, depth + 1));
                    beta = Math.max(beta, minValue);
                }
            }
            if (beta <= alpha) {
                return beta;
            }
        }
        return minValue;
    }

    private int minSecondMove(Game game, int depth) {

        if (depth == maxDepth) {
            return eval(game);
        }

        Player opp = getOpp(game);
        int minValue = Integer.MAX_VALUE;
        ArrayList<Action> actions = opp.getAllActions(game.getBoard());
        for (Action action : actions) {
            Game copyGame = game.copy();
            if (copyGame.applyActionTwo(opp, action, false)) {
                continue;
            }
            Player winner = copyGame.getWinner();
            if (winner != null) {
                if (winner.getType() == getType().reverse()) {
                    return Integer.MIN_VALUE;
                } else {
                    return Integer.MAX_VALUE;
                }
            } else {
                minValue = Math.min(minValue, maxForceAttack(copyGame, depth + 1));
                beta = Math.max(beta, minValue);
            }
            if (beta <= alpha) {
                return beta;
            }
        }
        return minValue;
    }

    public Player getOpp(Game game) {
        if (getType() == PlayerType.white) {
            return game.getBlack();
        } else {
            return game.getWhite();
        }
    }


}
