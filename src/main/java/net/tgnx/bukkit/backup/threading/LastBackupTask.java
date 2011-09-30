/*
 *  Backup - CraftBukkit server Backup plugin (continued)
 *  Copyright (C) 2011 Domenic Horner <https://github.com/gamerx/Backup>
 *  Copyright (C) 2011 Lycano <https://github.com/gamerx/Backup>
 *
 *  Backup - CraftBukkit server Backup plugin (original author)
 *  Copyright (C) 2011 Kilian Gaertner <https://github.com/Meldanor/Backup>
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

package net.tgnx.bukkit.backup.threading;

import net.tgnx.bukkit.backup.config.Settings;
import net.tgnx.bukkit.backup.utils.LogUtils;
import org.bukkit.Server;

/**
 *
 * @author Kilian Gaertner
 */
public class LastBackupTask extends PrepareBackupTask {

    private Server server = null;

    public LastBackupTask (Server server, Settings settings) {
        super(server, settings);
        this.server = server;
        this.strings = super.strings;
    }

    @Override
    public void run () {
        if (server.getOnlinePlayers().length <= 0) {
            LogUtils.sendLog(strings.getString("startlast"));
            super.prepareBackup();
        }
    }
}
