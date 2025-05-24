import Reflux from 'reflux'

import LanguageActions from './LanguageActions'

const data = {
  language: {
    key: "br",
    alt: "Brasil",
    title: "Português"
  },
  languages: [],
  phrases: {}
};

export default  class LanguageStore extends Reflux.Store {
  constructor() {
    super();
    this.listenToMany(LanguageActions);
    LanguageActions.init();
    LanguageActions.select(data.language);
  }

  getData = () => {
    return data
  }

  static translate(phrase) {
    return data.phrases[phrase] || phrase
  }

  onInitCompleted(_data) {
    data.languages = _data;
    this.trigger(data)
  }

  onSelectCompleted(_data) {
    data.phrases = _data;
    this.trigger(data)
  }

  setLanguage(_lang) {
    data.language = _lang
  }
}

