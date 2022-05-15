package go.lands.ilmis.utils.ugali

import go.lands.ilmis.model.locality.MdStreets
import go.lands.ilmis.model.locality.MdWards

class UgStatic{
    companion object {
        var lst_not = arrayListOf<Int>()
        var lp: MdWards.Lp? =null
        var cnf: MdStreets? = null
    }
}
