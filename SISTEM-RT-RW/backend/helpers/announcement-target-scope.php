<?php
declare(strict_types=1);

function announcementTargetScope(array $user): array
{
    $scopes = [];
    $parameters = [];

    if ($user['rt_id'] !== null) {
        $scopes[] = 'target.id_rt = :target_rt_id';
        $parameters['target_rt_id'] = (int) $user['rt_id'];
    }
    if ($user['rw_id'] !== null) {
        $scopes[] = 'target.id_rw = :target_rw_id';
        $parameters['target_rw_id'] = (int) $user['rw_id'];
        $scopes[] = 'target.id_rt IN (
            SELECT target_rt_scope.id FROM rt target_rt_scope
            WHERE target_rt_scope.id_rw = :target_rt_rw_id
        )';
        $parameters['target_rt_rw_id'] = (int) $user['rw_id'];
    } elseif ($user['rt_id'] !== null) {
        $scopes[] = 'target.id_rw = (
            SELECT target_rt_scope.id_rw FROM rt target_rt_scope
            WHERE target_rt_scope.id = :target_rt_owner_id
        )';
        $parameters['target_rt_owner_id'] = (int) $user['rt_id'];
    }

    $scope = $scopes === [] ? '1 = 1' : implode(' OR ', $scopes);

    return [
        'sql' => '(
            NOT EXISTS (
                SELECT 1 FROM target_pengumuman target
                WHERE target.id_pengumuman = p.id
            )
            OR EXISTS (
                SELECT 1 FROM target_pengumuman target
                WHERE target.id_pengumuman = p.id AND (' . $scope . ')
            )
        )',
        'parameters' => $parameters,
    ];
}
