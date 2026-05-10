package interfaces;

import services.TranslationService.Langue;
import services.TranslationService.ResultatTraduction;

public interface ITranslationService {
    ResultatTraduction traduire(String texte, Langue langueCible) throws Exception;
}
