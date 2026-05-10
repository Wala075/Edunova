package interfaces;

import services.ReputationService.Action;

public interface IReputationService {
    void ajouterPoints(int userId, Action action);
    int  getReputation(int userId);
}
