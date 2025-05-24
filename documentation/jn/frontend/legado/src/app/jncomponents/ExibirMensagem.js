import { smallBox } from '../components/utils/actions/MessageActions'

export default function exibirMensagem(detail, timeout) {
    smallBox({
        detail,
        timeout,
        content: "<br/><br/>" + detail,
        color: "#296191",
        icon: "fa fa-bell swing animated"
     });
   }
