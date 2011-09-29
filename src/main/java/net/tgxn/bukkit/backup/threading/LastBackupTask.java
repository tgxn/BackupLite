/*
 *  Backup - CraftBukkit server backup plugin. (continued 1.8+)
 *  @author Lycano <https://github.com/lycano/>
 * 
 *  Backup - CraftBukkit server backup plugin. (continued 1.7+)
 *  @author Domenic Horner <https://github.com/gamerx/>
 *
 *  Backup - CraftBukkit server backup plugin. (original author)
 *  @author Kilian Gaertner <https://github.com/Meldanor/>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.tgxn.bukkit.backup.threading;

import net.tgxn.bukkit.backup.config.Properties;
import org.bukkit.Server;

/**
 *
 * @author Kilian Gaertner
 */
public class LastBackupTask extends PrepareBackupTask {

    private Server server = null;

    public LastBackupTask (Server server, Properties properties) {
        super(server, properties);
        this.server = server;
        this.strings = super.strings;
    }

    @Override
    public void run () {
        if (server.getOnlinePlayers().length <= 0) {
            System.out.println(strings.getString("startlast"));
            super.prepareBackup();
        }
    }
}
